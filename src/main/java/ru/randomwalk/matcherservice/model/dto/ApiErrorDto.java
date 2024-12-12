package ru.randomwalk.matcherservice.model.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record ApiErrorDto (
        String message
) {}
