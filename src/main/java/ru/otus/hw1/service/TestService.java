package ru.otus.hw1.service;

import ru.otus.hw1.domain.Question;
import ru.otus.hw1.domain.Result;

import java.util.Set;

public interface TestService {

    Set<String> getAvailTests();

    void startTest(String testName, String firstname, String surname);

    boolean testIsExists(String testName);

    Question next();

    boolean hasNext();

    boolean doAnswer(Question question, String textAnswer);

    Result getResult();
}
