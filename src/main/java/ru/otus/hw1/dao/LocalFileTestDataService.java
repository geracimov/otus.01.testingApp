package ru.otus.hw1.dao;

import lombok.extern.java.Log;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
import java.util.stream.StreamSupport;

@Log
@Service
public class LocalFileTestDataService implements TestDataService {

    private final Path path;
    private final String globTemplate;
    private final String separator;
    private final String isCorrectSuffix;
    private volatile Map<String, Test> tests;

    @Autowired
    public LocalFileTestDataService(@Value("${csvPath}") Path path,
                                    @Value("${globTemplate}") String globTemplate,
                                    @Value("${separator}") String separator,
                                    @Value("${isCorrectSuffix}") String isCorrectSuffix) {
        this.path = path;
        this.globTemplate = globTemplate;
        this.separator = separator;
        this.isCorrectSuffix = isCorrectSuffix;
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
        if (testName == null || testName.isEmpty()) {
            return null;
        }
        return tests.get(testName);
    }

    private void loadTestList() {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, globTemplate)) {
                Stream<Path> pathStream = StreamSupport.stream(ds.spliterator(), false);
                pathStream.map(this::buildTest)
                          .filter(Objects::nonNull)
                          .forEach(test -> tests.put(test.getName(), test));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Test buildTest(Path file) {
        if (file == null) {
            return null;
        }
        try {
            List<Question> questions = Files.lines(file)
                                            .map(this::buildQuestion)
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toList());
            String testName = FilenameUtils.getBaseName(file.toString());
            return new Test(testName, questions);
        } catch (IOException e) {
            log.severe("Error parsing file: " + file.toString());
            return null;
        }
    }

    private Question buildQuestion(String str) {
        String[] description = str.split(this.separator);
        //в строке должен как минимум 1 вопрос и 1 ответ
        if (description.length < 2) {
            log.severe("Incorrect question format! Skip question: " + str);
            return null;
        }
        String[] questions = Arrays.copyOfRange(description, 1, description.length);

        List<Choice> choices = buildChoices(questions);
        return new Question(description[0], choices);
    }

    private List<Choice> buildChoices(String[] stringChoises) {
        return Stream.of(stringChoises)
                     .map(s -> {
                         boolean isCorrect = s.matches(".*" + isCorrectSuffix);
                         String text = isCorrect
                                       ? s.replaceAll(isCorrectSuffix, "")
                                       : s;
                         return new Choice(text, isCorrect);
                     })
                     .collect(Collectors.toList());
    }
}
