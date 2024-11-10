package ru.randomwalk.matcherservice.model.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ClubDto(
        UUID id,
        Boolean inFilter
) { }
