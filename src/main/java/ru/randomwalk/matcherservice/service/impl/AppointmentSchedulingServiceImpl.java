package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.model.AvailableTimeOverlapModel;
import ru.randomwalk.matcherservice.service.AppointmentDetailsService;
import ru.randomwalk.matcherservice.service.AppointmentSchedulingService;
import ru.randomwalk.matcherservice.service.AvailableTimeService;
import ru.randomwalk.matcherservice.service.PersonService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AppointmentSchedulingServiceImpl implements AppointmentSchedulingService {

    private final AvailableTimeService availableTimeService;
    private final AppointmentDetailsService appointmentDetailsService;
    private final MatcherProperties matcherProperties;
    private final PersonService personService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<AppointmentDetails> tryToScheduleAppointmentBetweenPeople(UUID personId, UUID partnerId) {
        try {
            Person person = personService.findByIdWithFetchedAvailableTime(personId);
            Person partner = personService.findByIdWithFetchedAvailableTime(partnerId);
            List<AvailableTimeOverlapModel> timeOverlaps = availableTimeService.getAllAvailableTimeOverlaps(
                    person.getAvailableTimes(),
                    partner.getAvailableTimes()
            );

            for (var overlap : timeOverlaps) {
                Optional<AppointmentDetails> details = scheduleAppointmentWithOverlap(person, partner, overlap);
                if (details.isPresent()) {
                    log.info("Appointment {} was scheduled for users: {} and {}", details.get().getId(), person.getId(), partner.getId());
                    return details;
                }
            }
        } catch (Exception e) {
            log.error("Error scheduling appointment for {} and {}", personId, partnerId, e);
        }

        return Optional.empty();
    }

    private Optional<AppointmentDetails> scheduleAppointmentWithOverlap(
            Person person,
            Person partner,
            AvailableTimeOverlapModel overlapModel
    ) {
        log.info("Scheduling appointment for person {} with partner {}", person.getId(), partner.getId());
        AvailableTime initialPersonAvailableTime = overlapModel.firstOverlappingAvailableTime();
        AvailableTime partnerAvailableTime = overlapModel.secondOverlappingAvailableTime();

        if (initialPersonAvailableTime == null || partnerAvailableTime == null) {
            log.warn(
                    "There is no available time for initial person {} and partner {} to schedule appointment with overlapModel = {}",
                    person.getId(), partner.getId(), overlapModel
            );
            return Optional.empty();
        }

        OffsetTime walkBeginTime = overlapModel.timeFrom();
        OffsetTime walkEndTime = calculateWalkEndTime(walkBeginTime);

        availableTimeService.decrementDayLimit(initialPersonAvailableTime.getDayLimit());
        availableTimeService.decrementDayLimit(partnerAvailableTime.getDayLimit());

        reserveAvailableTimeForWalk(person, initialPersonAvailableTime, walkBeginTime, walkEndTime);
        reserveAvailableTimeForWalk(partner, partnerAvailableTime, walkBeginTime, walkEndTime);

        OffsetDateTime startDateTime = getAppointmentStartDate(initialPersonAvailableTime, walkBeginTime);
        return Optional.of(appointmentDetailsService.createAppointment(person, partner, startDateTime));
    }

    private void reserveAvailableTimeForWalk(Person person, AvailableTime availableTime, OffsetTime walkBeginTime, OffsetTime walkEndTime) {
        var splitTime = availableTimeService.splitAvailableTime(availableTime, walkBeginTime, walkEndTime);
        availableTimeService.removeAvailableTimeForPerson(availableTime, person);
        person.getAvailableTimes().addAll(splitTime);
    }

    private OffsetTime calculateWalkEndTime(OffsetTime beginTime) {
        return beginTime
                .plusSeconds(matcherProperties.getMinWalkTimeInSeconds())
                .plusSeconds(matcherProperties.getOffsetBetweenWalksInSeconds());
    }

    private OffsetDateTime getAppointmentStartDate(AvailableTime availableTime, OffsetTime startTime) {
        ZoneOffset zoneOffset = ZoneOffset.of(availableTime.getTimezone());
        LocalDate date = availableTime.getDate();
        LocalTime localTime = startTime.toLocalTime();

        return OffsetDateTime.of(date, localTime, zoneOffset);
    }
}
