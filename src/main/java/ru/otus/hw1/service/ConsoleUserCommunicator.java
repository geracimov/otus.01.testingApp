package ru.otus.hw1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.otus.hw1.domain.Question;
import ru.otus.hw1.domain.Result;

import java.util.Locale;
import java.util.Scanner;

@Service
public class ConsoleUserCommunicator implements UserCommunicator {
    private final TestService service;
    private final Scanner scanner;
    private final MessageSource ms;
    private Locale locale;

    @Autowired
    public ConsoleUserCommunicator(TestService service, MessageSource messageSource) {
        this.service = service;
        this.ms = messageSource;
        this.scanner = new Scanner(System.in);
        this.locale = Locale.getDefault();
    }

    @Override
    public void startSession() {
        changeLocale();

        if (service.getAvailTests(locale) == null) {
            textDialog(message("test.not.found.in.path"));
            return;
        }

        boolean repeatTest = true;
        String y = message("test.repeat.y");
        String yes = message("test.repeat.yes");
        String n = message("test.repeat.n");
        String no = message("test.repeat.no");

        while (repeatTest) {
            textDialog(message("test.avail.list", service.getAvailTests(locale)));

            String testName = inputDialog(message("test.select"));
            if (!service.testIsExists(testName)) {
                textDialog(message("test.is.not.exists"));
                continue;
            }
            String surname = inputDialog(message("person.surname"));
            String firstname = inputDialog(message("person.firstname"));

            service.startTest(testName, firstname, surname);
            while (service.hasNext()) {
                Question q = service.next();
                boolean accepted = false;
                while (!accepted) {
                    showQuestion(q);
                    String textAnswer = inputDialog(message("answer.text"));
                    accepted = service.doAnswer(q, textAnswer);
                }
            }
            Result result = service.getResult();
            textDialog(message("result", result.getScore(), result.getPrc()));

            repeatTest = inputDialog(message("test.repeat") + " (" + y + "/" + n + ")").matches("(?i)" + y + "|" + yes);
        }
    }

    @Override
    public String getAnswer(String question) {
        return inputDialog(question);
    }

    @Override
    public void showQuestion(Question question) {
        if (question == null) {
            return;
        }
        textDialog(question.getText());
        for (int i = 0;
             i < question.getChoices()
                         .size();
             i++) {
            textDialog(String.format("%s) %s\n",
                                     i,
                                     question.getChoices()
                                             .get(i)
                                             .getText()));
        }
    }

    private void changeLocale() {
        textDialog(message("lang.current", locale));
        String lang = inputDialog(message("lang.change", locale.getDisplayLanguage()));

        if (!lang.isEmpty()) {
            locale = Locale.forLanguageTag(lang);
            textDialog(message("lang.changed"));
        }
    }

    private String inputDialog(String shownText) {
        textDialog(shownText + "> ");
        return scanner.nextLine();
    }

    private void textDialog(String shownText) {
        System.out.print("\n" + shownText);
    }

    private String message(String textBundle, Object... objects) {
        return ms.getMessage(textBundle, objects, locale);
    }
}
