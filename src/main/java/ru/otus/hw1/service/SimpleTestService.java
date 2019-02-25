package ru.otus.hw1.service;

import ru.otus.hw1.dao.TestDataService;
import ru.otus.hw1.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * логика подсчета баллов выдуманная:суммируется количество верно отмеченных
 * ответов, несмотря на неправильно отмеченные
 */
public class SimpleTestService implements TestService {


    private final TestDataService testDataService;
    private Test test;
    private Person person;
    private List<Answer> answers;

    public SimpleTestService(TestDataService testDataService) {
        this.testDataService = testDataService;
    }

    @Override
    public Set<String> getAvailTests() {
        return testDataService.getAvailTests();
    }

    @Override
    public void startTest(String testName, String firstname, String surname) {
        Test test = testDataService.getTest(testName);
        if (test == null) {
            throw new IllegalArgumentException("Выбрано неверное имя теста!");
        }
        this.test = test;
        this.person = new Person(firstname, surname);
        this.answers = new ArrayList<>();
    }

    @Override
    public Question next() {
        if (!hasNext()) {
            calcResult();
            return null;
        }
        return this.test.getQuestions()
                        .get(answers.size());
    }

    @Override
    public boolean hasNext() {
        return answers.size() < this.test.getQuestions()
                                         .size();
    }

    @Override
    public boolean doAnswer(Question question, String textAnswer) {
        try {
            List<Choice> choices = Stream.of(textAnswer.split("[\\s]"))
                                         .mapToInt(Integer::parseInt)
                                         .mapToObj(i -> question.getChoices()
                                                                .get(i))
                                         .collect(Collectors.toList());


            answers.add(new Answer(question, choices));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Result getResult() {
        if (hasNext()) {
            throw new IllegalStateException("Тест еще не завершен!");
        }
        return calcResult();
    }


    private Result calcResult() {
        int score = 0;
        for (Answer answer : answers) {
            score += answer.getChoices()
                           .stream()
                           .mapToInt(choice -> choice.isCorrect()
                                               ? 1
                                               : 0)
                           .sum();
        }
        int totalScore = totalScore(this.test);
        double prc = roundAvoid(100.0 * score / totalScore, 2);

        return new Result(person, score, prc);
    }

    private int totalScore(Test test) {
        return test.getQuestions()
                   .stream()
                   .mapToInt(this::totalScore)
                   .sum();
    }

    private int totalScore(Question question) {
        return question.getChoices()
                       .stream()
                       .mapToInt(choice -> choice.isCorrect()
                                           ? 1
                                           : 0)
                       .sum();
    }

    private double roundAvoid(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}
