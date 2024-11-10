package ru.randomwalk.matcherservice.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record AvailableTimeRequestDto(
        @NotNull
        LocalDate date,
        @NotNull
        Integer timezone,
        Integer walkCount,
        @NotEmpty
        List<TimeFrame> timeFrames
) {
    @Builder
    public record TimeFrame(
            @NotNull
            LocalTime timeFrom,
            @NotNull
            LocalTime timeUntil
    ){}
}
