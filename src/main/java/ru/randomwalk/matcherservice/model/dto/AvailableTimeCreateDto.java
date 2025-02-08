package ru.randomwalk.matcherservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record AvailableTimeCreateDto(
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
        Double longitude,
        @NotNull
        Double latitude,
        List<UUID> clubsInFilter
) {
        public AvailableTimeCreateDto(
                LocalDate date,
                OffsetTime timeFrom, @NotNull
                OffsetTime timeUntil,
                Double longitude,
                Double latitude,
                List<UUID> clubsInFilter
        ) {
                this.date = date;
                this.timeFrom = timeFrom;
                this.timeUntil = timeUntil;
                this.longitude = longitude;
                this.latitude = latitude;
                this.clubsInFilter = Objects.requireNonNullElseGet(clubsInFilter, ArrayList::new);
        }
}
