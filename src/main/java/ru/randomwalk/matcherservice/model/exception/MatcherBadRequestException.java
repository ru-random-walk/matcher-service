package ru.randomwalk.matcherservice.model.exception;

public class MatcherBadRequestException extends RuntimeException{
    public MatcherBadRequestException() {
    }

    public MatcherBadRequestException(String message) {
        super(message);
    }

    public MatcherBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
