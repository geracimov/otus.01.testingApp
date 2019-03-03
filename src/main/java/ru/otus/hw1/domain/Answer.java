package ru.otus.hw1.domain;

import lombok.Data;

import java.util.List;

@Data
public class Answer {
    private final Question question;
    private final List<Choice> choices;
}
