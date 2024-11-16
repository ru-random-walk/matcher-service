package ru.randomwalk.matcherservice.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;


@Builder
public record LocationDto(
        String region,
        String city,
        String country,
        String street,
        String house,
        @NotNull
        Double longitude,
        @NotNull
        Double latitude,
        Integer searchAreaMeters
) {
}
