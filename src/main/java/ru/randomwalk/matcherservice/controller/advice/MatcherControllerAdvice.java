package ru.randomwalk.matcherservice.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.randomwalk.matcherservice.model.dto.ApiErrorDto;
import ru.randomwalk.matcherservice.model.exception.MatcherBadRequestException;
import ru.randomwalk.matcherservice.model.exception.MatcherForbiddenException;
import ru.randomwalk.matcherservice.model.exception.MatcherNotFoundException;

@RestControllerAdvice
@Slf4j
public class MatcherControllerAdvice {

    @ExceptionHandler({MatcherBadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrorDto> exceptionHandler(MatcherBadRequestException e) {
        log.debug("Handle bad request exception", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDto(e.getMessage()));
    }

    @ExceptionHandler({MatcherNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiErrorDto> exceptionHandler(MatcherNotFoundException e) {
        log.debug("Not found exception", e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorDto(e.getMessage()));
    }

    @ExceptionHandler({MatcherForbiddenException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiErrorDto> exceptionHandler(MatcherForbiddenException e) {
        log.debug("Forbidden exception", e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorDto(e.getMessage()));
    }
}
