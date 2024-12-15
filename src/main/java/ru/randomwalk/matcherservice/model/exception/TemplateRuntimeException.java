package ru.randomwalk.matcherservice.model.exception;

public class TemplateRuntimeException extends RuntimeException{

    public TemplateRuntimeException(String message, Object... args) {
        super(String.format(message, args));
    }

    public TemplateRuntimeException() {
    }

    public TemplateRuntimeException(String message) {
        super(message);
    }

    public TemplateRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateRuntimeException(Throwable cause) {
        super(cause);
    }
}
