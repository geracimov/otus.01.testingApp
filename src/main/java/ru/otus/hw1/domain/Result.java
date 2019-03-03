package ru.otus.hw1.domain;

import lombok.Data;

@Data
public class Result {
    private final Person person;
    private final int score;
    private final double prc;
}

