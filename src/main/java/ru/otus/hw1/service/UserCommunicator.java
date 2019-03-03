package ru.otus.hw1.service;

import ru.otus.hw1.domain.Question;

public interface UserCommunicator {

    void startSession();

    String getAnswer(String question);

    void showQuestion(Question question);
}
