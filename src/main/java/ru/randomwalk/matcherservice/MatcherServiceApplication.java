package ru.randomwalk.matcherservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MatcherServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatcherServiceApplication.class, args);
    }

}
