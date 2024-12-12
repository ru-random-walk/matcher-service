package ru.randomwalk.matcherservice.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record AppointmentRequestDto(
        @Valid
        @NotNull
        List<AvailableTimeRequestDto> availableTime
) {
}
