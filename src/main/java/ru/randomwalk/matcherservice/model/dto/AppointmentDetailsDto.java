package ru.randomwalk.matcherservice.model.dto;

import lombok.Builder;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record AppointmentDetailsDto(
        UUID id,
        List<UUID> participants,
        LocalDateTime startsAt,
        LocalDateTime endedAt,
        AppointmentStatus status
) {
}
