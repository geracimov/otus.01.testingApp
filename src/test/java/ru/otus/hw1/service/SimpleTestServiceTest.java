package ru.otus.hw1.service;

import org.junit.Before;
import org.junit.Test;
import ru.otus.hw1.dao.LocalFileTestDataService;
import ru.otus.hw1.dao.TestDataService;
import ru.otus.hw1.domain.Choice;
import ru.otus.hw1.domain.Question;
import ru.otus.hw1.domain.Result;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimpleTestServiceTest {
    private Set<String> testNames;
    private List<Choice> someChoices1, someChoices2, someChoices3;
    private Question someQuestion1, someQuestion2, someQuestion3;
    private ru.otus.hw1.domain.Test someTest;
    private TestService ts;

    @Before
    public void init() {
        testNames = Stream.of("test1", "test2", "test3")
                          .collect(Collectors.toSet());
        someChoices1 = Arrays.asList(new Choice("case1", true),
                                     new Choice("case2", false),
                                     new Choice("case3", true));
        someQuestion1 = new Question("question1", someChoices1);

        someChoices2 = Arrays.asList(new Choice("case1", false),
                                     new Choice("case2", true));
        someQuestion2 = new Question("question2", someChoices2);

        someChoices3 = Arrays.asList(new Choice("case1", true),
                                     new Choice("case2", false));
        someQuestion3 = new Question("question3", someChoices3);

        List<Question> questions =
                Stream.of(someQuestion1, someQuestion2, someQuestion3)
                      .collect(Collectors.toList());
        someTest = new ru.otus.hw1.domain.Test("test2", questions);

        TestDataService tds = mock(LocalFileTestDataService.class);
        when(tds.getAvailTests()).thenReturn(new HashSet<>(testNames));
        when(tds.getTest(anyString())).thenReturn(someTest);
        ts = new SimpleTestService(tds);
    }

    @Test
    public void getAvailTests() {
        assertEquals("Sets of tests are not equals",
                     ts.getAvailTests(),
                     testNames);
    }

    @Test(expected = NullPointerException.class)
    public void getNextBeforeStartTest() {
        ts.hasNext();
        ts.next();
    }

    @Test
    public void getNextAfterStartTest() {
        ts.startTest("test2", "firstname", "surname");
        assertTrue(ts.hasNext());
        assertThat(ts.next(), instanceOf(Question.class));
    }

    @Test
    public void getMoreNextQuestionWithoutAnswer() {
        ts.startTest("test2", "firstname", "surname");
        assertEquals(ts.next(), someQuestion1);
        assertEquals(ts.next(), someQuestion1);
        assertEquals(ts.next(), someQuestion1);
    }

    @Test
    public void getMoreNextQuestionWithAnswerToFinish() {
        ts.startTest("test2", "firstname", "surname");

        Question question = ts.next();
        assertEquals(question, someQuestion1);
        assertTrue(ts.doAnswer(question, "1"));

        question = ts.next();
        assertEquals(question, someQuestion2);
        assertFalse(ts.doAnswer(question, "aaa"));
        assertFalse(ts.doAnswer(question, "99"));
        assertTrue(ts.doAnswer(question, "0"));

        question = ts.next();
        assertEquals(question, someQuestion3);
        assertTrue(ts.doAnswer(question, "1"));

        assertFalse(ts.hasNext());
        assertNull(ts.next());
    }

    @Test(expected = NullPointerException.class)
    public void getResultBeforeFinish() {
        ts.getResult();
    }

    @Test
    public void getResultAfterFinish() {
        ts.startTest("test2", "firstname", "surname");

        while (ts.hasNext()) {
            Question question = ts.next();
            ts.doAnswer(question, "0");
        }
        Result res = ts.getResult();
        assertNotNull(res);
        assertEquals(res.getScore(), 2);
    }
}