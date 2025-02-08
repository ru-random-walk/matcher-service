package ru.randomwalk.matcherservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;

import java.time.OffsetTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ScheduleTimeFrameDto(
        @Nullable
        UUID partnerId,
        @Nullable
        UUID appointmentId,
        @Schema(example = "01:30:00.000+03:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        OffsetTime timeFrom,
        @Schema(example = "01:30:00.000+03:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        OffsetTime timeUntil,
        Double latitude,
        Double longitude,
        List<UUID> availableTimeClubsInFilter,
        AppointmentStatus appointmentStatus
) {
}
