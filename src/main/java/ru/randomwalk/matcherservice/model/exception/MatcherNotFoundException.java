package ru.randomwalk.matcherservice.model.exception;

public class MatcherNotFoundException extends TemplateRuntimeException{

    public MatcherNotFoundException(String message, Object... args) {
        super(message, args);
    }

    public MatcherNotFoundException(String message) {
        super(message);
    }

    public MatcherNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
