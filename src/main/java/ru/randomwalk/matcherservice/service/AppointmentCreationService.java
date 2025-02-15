package ru.randomwalk.matcherservice.service;

import org.apache.commons.lang3.tuple.Pair;
import ru.randomwalk.matcherservice.model.dto.AppointmentCreationResultDto;
import ru.randomwalk.matcherservice.model.dto.TimePeriod;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;

import java.time.OffsetTime;

public interface AppointmentCreationService {

    AppointmentCreationResultDto createAppointmentForAvailableTime(
            AvailableTime availableTime,
            AvailableTime matchingTime,
            OffsetTime startTime
    );
}
