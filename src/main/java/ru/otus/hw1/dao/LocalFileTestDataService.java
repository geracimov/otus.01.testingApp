package ru.otus.hw1.dao;

import lombok.extern.java.Log;
import org.apache.commons.io.FilenameUtils;
import ru.otus.hw1.domain.Choice;
import ru.otus.hw1.domain.Question;
import ru.otus.hw1.domain.Test;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log
public class LocalFileTestDataService implements TestDataService {

    private final Path path;
    private final String globTemplate;
    private final String separator;
    private final String correctSuffix;
    private volatile Map<String, Path> tests;

    public LocalFileTestDataService(Path path,
                                    String globTemplate,
                                    String separator,
                                    String correctSuffix) {
        this.path = path;
        this.globTemplate = globTemplate;
        this.separator = separator;
        this.correctSuffix = correctSuffix;
        tests = new HashMap<>();
        loadTestList();
    }

    @Override
    public Set<String> getAvailTests() {
        if (tests.keySet()
                 .size() > 0) {
            return tests.keySet();
        }
        return null;
    }

    @Override
    public Test getTest(String testName) {
        if (testName == null
            || !testName.isEmpty()
            return null;
        }
        Path path = tests.get(testName);

        try {
            List<Question> questions = Files.lines(path)
                                            .map(this::buildQuestion)
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toList());
            return new Test(testName, questions);
        } catch (IOException e) {
            log.severe("Ошибка чтения файла теста: " + e.getLocalizedMessage());
            return null;
        }
    }

    private void loadTestList() {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path,
                                                                         globTemplate)) {
                stream.forEach(file -> tests.put(FilenameUtils.getBaseName(file.toString()),
                                                 file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Question buildQuestion(String str) {
        String[] description = str.split(this.separator);
        //в строке должен как минимум 1 вопрос и 1 ответ
        if (description.length < 2) {
            log.severe("Неверный формат описания вопроса ! " + str);
            return null;
        }

        List<Choice> choices = buildChoices(description);
        return new Question(description[0], choices);
    }

    private List<Choice> buildChoices(String[] stringChoises) {
        return Stream.of(stringChoises)
                     .skip(1) //пропускаем текст вопроса
                     .map(s -> {
                         boolean isCorrect = s.matches(correctSuffix);
                         String text = isCorrect
                                       ? s.replaceAll(" *\\* *$", "")
                                       : s;
                         return new Choice(text, isCorrect);
                     })
                     .collect(Collectors.toList());
    }
}
