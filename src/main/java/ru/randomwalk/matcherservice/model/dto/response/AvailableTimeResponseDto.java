package ru.randomwalk.matcherservice.model.dto.response;

import jakarta.annotation.Nullable;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AvailableTimeResponseDto(
       LocalDate date,
       Integer timezone,
       Integer walkCount,
       List<AppointmentTimeFrameResponseDto> timeFrames
) {

}
