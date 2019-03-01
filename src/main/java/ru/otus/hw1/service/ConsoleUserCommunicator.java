package ru.otus.hw1.service;

import ru.otus.hw1.domain.Question;

import java.util.Scanner;

public class ConsoleUserCommunicator implements UserCommunicator {
    private final TestService service;
    private final Scanner scanner;

    public ConsoleUserCommunicator(TestService service) {
        this.service = service;
        scanner = new Scanner(System.in);
    }

    @Override
    public void startSession() {
        if (service.getAvailTests() == null) {
            System.out.println("In path in ENV (DATACSV) tests not found!");
            return;
        }

        boolean repeatTest = true;
        while (repeatTest) {
            System.out.println("Available tests:\n" + service.getAvailTests());
            String testName = getAnswer("Select the test");
            if (!service.testIsExists(testName)) {
                System.err.println("\nTest is not exists. Enter correct test name!");
                continue;
            }
            String surname = getAnswer("Your surname");
            String firstname = getAnswer("Your firstname");

            service.startTest(testName, firstname, surname);
            while (service.hasNext()) {
                Question q = service.next();
                boolean accepted = false;
                while (!accepted) {
                    showQuestion(q);
                    String textAnswer = getAnswer("Your answer");
                    accepted = service.doAnswer(q, textAnswer);
                }
            }
            System.out.println(service.getResult());
            repeatTest = getAnswer("Do you want to repeat again? (y/n)").matches("(?i)y|yes");
        }
    }

    @Override
    public String getAnswer(String question) {
        System.out.println("\n" + question + ": ");
        System.out.print("> ");
        return scanner.nextLine();
    }

    @Override
    public void showQuestion(Question question) {
        if (question == null) {
            return;
        }
        System.out.println(question.getText());
        for (int i = 0;
             i < question.getChoices()
                         .size();
             i++) {
            System.out.printf("%s) %s\n",
                              i,
                              question.getChoices()
                                      .get(i)
                                      .getText());
        }
    }
}
