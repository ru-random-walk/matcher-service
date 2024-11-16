package ru.randomwalk.matcherservice.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record AppointmentRequestDto(
        @NotNull
        List<AvailableTimeRequestDto> availableTime
) {
}
