package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.dto.AppointmentCreationResultDto;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.service.AppointmentCreationService;
import ru.randomwalk.matcherservice.service.AppointmentDetailsService;
import ru.randomwalk.matcherservice.service.AvailableTimeService;
import ru.randomwalk.matcherservice.service.DayLimitService;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AppointmentCreationServiceImpl implements AppointmentCreationService {

    private final AvailableTimeService availableTimeService;
    private final AppointmentDetailsService appointmentDetailsService;
    private final MatcherProperties matcherProperties;
    private final DayLimitService dayLimitService;

    @Override
    public AppointmentCreationResultDto createAppointmentForAvailableTime(
            AvailableTime availableTime,
            AvailableTime matchingTime,
            OffsetTime startTime
    ) {
        UUID personId = availableTime.getPersonId();
        UUID partnerId = availableTime.getPersonId();
        log.info(
                "Creating appointment for people: {} with availableTimeId={} and {} with availableTimeId={}",
                personId, partnerId, availableTime.getId(), matchingTime.getId()
        );

        OffsetTime walkEndTime = calculateWalkEndTime(startTime);

        dayLimitService.decrementDayLimitForAvailableTime(availableTime);
        dayLimitService.decrementDayLimitForAvailableTime(matchingTime);

        OffsetDateTime startDateTime = getAppointmentStartDate(availableTime, startTime);
        Point appointmentLocation = getApproximateAppointmentLocation(availableTime, matchingTime);

        var appointment = appointmentDetailsService.createAppointment(personId, partnerId, startDateTime, appointmentLocation);
        List<AvailableTime> initialTimeSplit = availableTimeService.splitAvailableTime(availableTime, startTime, walkEndTime);
        List<AvailableTime> matchingTimeSplit = availableTimeService.splitAvailableTime(availableTime, startTime, walkEndTime);

        return new AppointmentCreationResultDto(appointment, initialTimeSplit, matchingTimeSplit);
    }

    private Point getApproximateAppointmentLocation(AvailableTime availableTime, AvailableTime matchingTime) {
        double approximateLatitude = (availableTime.getLatitude() + matchingTime.getLatitude()) / 2;
        double approximateLongitude = (availableTime.getLongitude() + matchingTime.getLongitude()) / 2;

        return GeometryUtil.createPoint(approximateLongitude, approximateLatitude);
    }

    private OffsetTime calculateWalkEndTime(OffsetTime beginTime) {
        return beginTime
                .plusSeconds(matcherProperties.getMinWalkTimeInSeconds());
    }

    private OffsetDateTime getAppointmentStartDate(AvailableTime availableTime, OffsetTime startTime) {
        ZoneOffset zoneOffset = ZoneOffset.of(availableTime.getTimezone());
        LocalDate date = availableTime.getDate();
        LocalTime localTime = startTime.toLocalTime();

        return OffsetDateTime.of(date, localTime, zoneOffset);
    }
}
