package ru.randomwalk.matcherservice.model.exception;

public class MatcherBadRequestException extends TemplateRuntimeException{
    public MatcherBadRequestException() {
    }

    public MatcherBadRequestException(String message, Object... args) {
        super(message, args);
    }

    public MatcherBadRequestException(String message) {
        super(message);
    }

    public MatcherBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
