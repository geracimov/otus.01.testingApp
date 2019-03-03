package ru.otus.hw1.domain;

import lombok.Data;

import java.util.List;


@Data
public class Test {
    private final String name;
    private final List<Question> questions;
    private List<Result> results;
}
