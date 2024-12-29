package ru.randomwalk.matcherservice.model.dto.request;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AddPersonDto(
        UUID id,
        String fullName,
        Integer age,
        String gender
) {}
