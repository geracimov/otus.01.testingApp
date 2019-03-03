package ru.otus.hw1;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.otus.hw1.service.UserCommunicator;

public class TestingApp {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("/beans.xml");
        UserCommunicator uc = context.getBean(UserCommunicator.class);

        uc.startSession();
    }
}
