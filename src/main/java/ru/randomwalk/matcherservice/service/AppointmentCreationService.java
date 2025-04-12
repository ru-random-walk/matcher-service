package ru.randomwalk.matcherservice.service;

import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Point;
import ru.randomwalk.matcherservice.model.dto.AppointmentCreationResultDto;
import ru.randomwalk.matcherservice.model.dto.TimePeriod;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.UUID;

public interface AppointmentCreationService {

    AppointmentCreationResultDto createAppointmentForAvailableTime(
            AvailableTime availableTime,
            AvailableTime matchingTime,
            OffsetTime startTime
    );

    AppointmentDetails createRequestedAppointment(
            UUID requesterId,
            UUID partnerId,
            OffsetDateTime startsAt,
            Point location
    );
}
