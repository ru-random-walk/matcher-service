package ru.randomwalk.matcherservice.model.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record UserScheduleDto(
       LocalDate date,
       String timezone,
       Integer walkCount,
       List<ScheduleTimeFrameDto> timeFrames
) {

}
