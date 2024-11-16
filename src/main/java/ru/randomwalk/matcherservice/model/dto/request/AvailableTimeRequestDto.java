package ru.randomwalk.matcherservice.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.List;

@Builder
public record AvailableTimeRequestDto(
        @NotNull
        LocalDate date,
        Integer walkCount,
        @NotEmpty
        List<TimeFrame> timeFrames
) {
    @Builder
    public record TimeFrame(
            @NotNull
            OffsetTime timeFrom,
            @NotNull
            OffsetTime timeUntil
    ){}
}
