package ru.randomwalk.matcherservice.model.dto;

import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;

import java.util.List;

public record AppointmentCreationResultDto(
        AppointmentDetails appointmentDetails,
        List<AvailableTime> initialAvailableTimeSplit,
        List<AvailableTime> matchingAvailableTimeSplit
) {
}
