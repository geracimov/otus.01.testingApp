package ru.otus.hw1.domain;

import lombok.Data;

import java.util.List;

@Data
public class Question {
    private final String text;
    private final List<Choice> choices;
}
