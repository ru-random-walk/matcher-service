package ru.randomwalk.matcherservice.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LocationDto(
        @NotNull
        double longitude,
        @NotNull
        double latitude,
        @NotNull
        String city,
        @NotNull
        String street,
        @NotNull
        String building
) {
}
