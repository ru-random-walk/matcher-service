package ru.randomwalk.matcherservice.model.exception;

public class MatcherNotFoundException extends RuntimeException{

    public MatcherNotFoundException(String message) {
        super(message);
    }

    public MatcherNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
