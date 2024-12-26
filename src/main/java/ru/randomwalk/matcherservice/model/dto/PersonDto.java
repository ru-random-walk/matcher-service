package ru.randomwalk.matcherservice.model.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record PersonDto(
        UUID id,
        Integer age,
        String gender,
        LocationDto currentPosition,
        List<ClubDto> clubs
) {
}
