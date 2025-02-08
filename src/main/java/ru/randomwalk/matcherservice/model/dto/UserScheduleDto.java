package ru.randomwalk.matcherservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record UserScheduleDto(
       LocalDate date,
       @Schema(example = "+03:00")
       String timezone,
       Integer walkCount,
       List<ScheduleTimeFrameDto> timeFrames
) {

}
