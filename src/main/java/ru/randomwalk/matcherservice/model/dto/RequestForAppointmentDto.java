package ru.randomwalk.matcherservice.model.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RequestForAppointmentDto(
        UUID requesterId,
        UUID partnerId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        OffsetDateTime startTime,
        Double longitude,
        Double latitude
) {

}
