package ru.otus.hw1;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import ru.otus.hw1.service.UserCommunicator;

@Configuration
@ComponentScan
@PropertySource("classpath:application.properties")
public class TestingApp {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestingApp.class);
        UserCommunicator uc = context.getBean(UserCommunicator.class);

        uc.startSession();
    }
}
