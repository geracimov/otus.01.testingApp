package ru.otus.hw1.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.otus.hw1.dao.TestDataService;
import ru.otus.hw1.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * логика подсчета баллов выдуманная:суммируется количество верно отмеченных
 * ответов, несмотря на неправильно отмеченные
 */
@Log
@Service
public class SimpleTestService implements TestService {

    private final TestDataService testDataService;
    private Test test;
    private Person person;
    private List<Answer> answers;
    private int currQuestionIndex;

    @Autowired
    public SimpleTestService(TestDataService testDataService) {
        this.testDataService = testDataService;
    }

    @Override
    public Set<String> getAvailTests(Locale locale) {
        return testDataService.getAvailTests()
                              .stream()
                              .filter(t -> t.matches(".+_" + locale.getLanguage()))
                              .collect(Collectors.toSet());
    }

    @Override
    public void startTest(String testName, String firstname, String surname) {
        Test test = testDataService.getTest(testName);
        if (test == null) {
            throw new IllegalArgumentException("Incorrect test name!");
        }
        this.test = test;
        this.person = new Person(firstname, surname);
        this.answers = new ArrayList<>();
    }

    @Override
    public boolean testIsExists(String testName) {
        return testDataService.getTest(testName) != null;
    }

    @Override
    public Question next() {
        if (!hasNext()) {
            calcResult();
            return null;
        }
        return this.test.getQuestions()
                        .get(currQuestionIndex++);
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
            log.severe(String.format("Error during answer the question/answer (%s)/(%s)", question, answers));
            return false;
        }
    }

    @Override
    public Result getResult() {
        if (hasNext()) {
            throw new IllegalStateException("Test is not comleted!");
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
