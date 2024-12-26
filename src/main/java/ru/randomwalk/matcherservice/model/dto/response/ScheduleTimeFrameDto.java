package ru.randomwalk.matcherservice.model.dto.response;

import jakarta.annotation.Nullable;
import lombok.Builder;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;

import java.time.OffsetTime;
import java.util.UUID;

@Builder
public record ScheduleTimeFrameDto(
        @Nullable
        UUID partnerId,
        @Nullable
        UUID appointmentId,
        OffsetTime timeFrom,
        OffsetTime timeUntil,
        AppointmentStatus appointmentStatus
) {
}
