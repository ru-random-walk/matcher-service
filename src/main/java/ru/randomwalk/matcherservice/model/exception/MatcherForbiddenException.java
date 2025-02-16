package ru.randomwalk.matcherservice.model.exception;

public class MatcherForbiddenException extends TemplateRuntimeException{
    public MatcherForbiddenException(String message, Object... args) {
        super(message, args);
    }

    public MatcherForbiddenException() {
        super("Access is forbidden");
    }

    public MatcherForbiddenException(String message) {
        super(message);
    }
}
