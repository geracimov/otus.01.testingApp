package ru.otus.hw1.dao;

import ru.otus.hw1.domain.Test;

import java.util.Set;

public interface TestDataService {

    Set<String> getAvailTests();

    Test getTest(String testName);
}
