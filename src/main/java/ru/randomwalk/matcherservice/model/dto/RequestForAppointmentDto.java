package ru.randomwalk.matcherservice.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RequestForAppointmentDto(
        UUID requesterId,
        UUID partnerId,
        OffsetDateTime startTime,
        Double longitude,
        Double latitude
) {

}
