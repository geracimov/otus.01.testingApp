package ru.otus.hw1;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.otus.hw1.domain.Question;
import ru.otus.hw1.service.TestService;

import java.util.Scanner;

public class TestingApp {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("/beans.xml");
        TestService service = context.getBean(TestService.class);
        Scanner scanner = new Scanner(System.in);
        if (service.getAvailTests() == null) {
            System.out.println(
                    "В директории переменной окружения DATACSV тестов не найдено!");
            return;
        }

        boolean repeatTest = true;
        while (repeatTest) {
            System.out.println("Available tests:\n" + service.getAvailTests());
            System.out.print("\nSelect the test: ");
            String textName = scanner.nextLine();
            System.out.print("\nYour surname: ");
            String surname = scanner.nextLine();
            System.out.print("\nYour  firstname: ");
            String firstname = scanner.nextLine();


            service.startTest(textName, firstname, surname);
            while (service.hasNext()) {
                Question q = service.next();
                boolean accepted = false;
                while (!accepted) {
                    printQuestion(q);
                    System.out.print("> ");
                    String textAnswer = scanner.nextLine();
                    accepted = service.doAnswer(q, textAnswer);
                }
            }
            System.out.println(service.getResult());

            System.out.println("Do you want to repeat again? (y/n)");
            repeatTest = scanner.nextLine()
                                .matches("(?i)y|yes");

        }
    }

    private static void printQuestion(Question question) {
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
