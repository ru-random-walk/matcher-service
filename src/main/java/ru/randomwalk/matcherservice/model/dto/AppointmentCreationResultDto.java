package ru.randomwalk.matcherservice.model.dto;

import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;

import java.util.List;
import java.util.UUID;

public record AppointmentCreationResultDto(
        AppointmentDetails appointmentDetails,
        List<AvailableTime> initialAvailableTimeSplit,
        List<AvailableTime> matchingAvailableTimeSplit,
        UUID initialPersonId,
        UUID partnerId
) {
}
