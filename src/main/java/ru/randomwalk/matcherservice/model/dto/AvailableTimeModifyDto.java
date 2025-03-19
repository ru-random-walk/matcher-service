package ru.randomwalk.matcherservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record AvailableTimeModifyDto(
        @NotNull
        LocalDate date,
        @NotNull
        @Schema(example = "01:30:00.000+03:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        OffsetTime timeFrom,
        @NotNull
        @Schema(example = "01:30:00.000+03:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        OffsetTime timeUntil,
        @NotNull
        LocationDto location,
        List<UUID> clubsInFilter
) {
        public AvailableTimeModifyDto(
                LocalDate date,
                OffsetTime timeFrom,
                OffsetTime timeUntil,
                LocationDto location,
                List<UUID> clubsInFilter
        ) {
                this.date = date;
                this.timeFrom = timeFrom;
                this.timeUntil = timeUntil;
                this.location = location;
                this.clubsInFilter = Objects.requireNonNullElseGet(clubsInFilter, ArrayList::new);
        }
}
