package ru.randomwalk.matcherservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.List;

@Builder
public record AvailableTimeRequestDto(
        @NotNull
        LocalDate date,
        Integer walkCount,
        @Valid
        @NotEmpty
        List<TimeFrame> timeFrames
) {
    @Builder
    public record TimeFrame(
            @NotNull
            @Schema(example = "01:30:00.000+03:00")
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            OffsetTime timeFrom,
            @NotNull
            @Schema(example = "01:30:00.000+03:00")
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            OffsetTime timeUntil
    ){}
}
