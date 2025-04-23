package ru.randomwalk.matcherservice.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.randomwalk.matcherservice.AbstractContainerTest;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.DayLimit;
import ru.randomwalk.matcherservice.model.entity.Location;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.repository.AvailableTimeRepository;
import ru.randomwalk.matcherservice.repository.DayLimitRepository;
import ru.randomwalk.matcherservice.service.AppointmentDetailsService;
import ru.randomwalk.matcherservice.service.AvailableTimeService;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(MockitoExtension.class)
class AppointmentDetailsServiceImplTest extends AbstractContainerTest {

    @Autowired
    private AppointmentDetailsService appointmentDetailsService;
    @Autowired
    private AvailableTimeService availableTimeService;
    @Autowired
    private DayLimitRepository dayLimitRepository;
    @Autowired
    private MatcherProperties matcherProperties;
    @Autowired
    private PersonService personService;

    private final static Point location = GeometryUtil.createPoint(1.0, 2.0);

    @Transactional
    @Rollback
    @Test
    void approveRequestedAppointment() {
        UUID firstPersonId = UUID.randomUUID();
        UUID secondPersonId = UUID.randomUUID();
        personService.addNewPerson(RegisteredUserInfoEvent.builder().id(firstPersonId).build());
        personService.addNewPerson(RegisteredUserInfoEvent.builder().id(secondPersonId).build());
        var requestedAppointment = appointmentDetailsService.requestForAppointment(firstPersonId, secondPersonId, OffsetDateTime.now(ZoneId.of("UTC")), location);
        addAvailableTimeForPerson(
                firstPersonId,
                location,
                requestedAppointment.getStartsAt().toLocalTime().minusHours(2),
                requestedAppointment.getStartsAt().toLocalTime().plusHours(5),
                1
        );
        addAvailableTimeForPerson(
                secondPersonId,
                location,
                requestedAppointment.getStartsAt().toLocalTime().minusHours(2),
                requestedAppointment.getStartsAt().toLocalTime().plusSeconds(matcherProperties.getMinWalkTimeInSeconds()),
                1
        );

        appointmentDetailsService.approveRequestedAppointment(requestedAppointment);

        assertEquals(AppointmentStatus.APPOINTED, requestedAppointment.getStatus());
        Person first = personService.findByIdWithFetchedAvailableTime(firstPersonId);
        Person second = personService.findByIdWithFetchedAvailableTime(secondPersonId);
        assertEquals(2, first.getAvailableTimes().size());
        assertEquals(1, second.getAvailableTimes().size());
        assertEquals(0, dayLimitRepository.findById(new DayLimit.DayLimitId(firstPersonId, requestedAppointment.getStartDate())).get().getWalkCount());
        assertEquals(0, dayLimitRepository.findById(new DayLimit.DayLimitId(secondPersonId, requestedAppointment.getStartDate())).get().getWalkCount());
    }

    @Transactional
    @Rollback
    @Test
    void rejectRequestedAppointment() {
        UUID firstPersonId = UUID.randomUUID();
        UUID secondPersonId = UUID.randomUUID();
        personService.addNewPerson(RegisteredUserInfoEvent.builder().id(firstPersonId).build());
        personService.addNewPerson(RegisteredUserInfoEvent.builder().id(secondPersonId).build());
        var requestedAppointment = appointmentDetailsService.requestForAppointment(firstPersonId, secondPersonId, OffsetDateTime.now(ZoneId.of("UTC")), location);

        appointmentDetailsService.rejectRequestedAppointment(requestedAppointment);

        assertEquals(AppointmentStatus.CANCELED, requestedAppointment.getStatus());
    }

    private AvailableTime addAvailableTimeForPerson(UUID personId, Point point, LocalTime from, LocalTime to, Integer dayLimit) {
        AvailableTime availableTime = new AvailableTime();

        availableTime.setDate(LocalDate.now());
        availableTime.setTimeFrom(OffsetTime.of(from, ZoneOffset.UTC));
        availableTime.setTimeUntil(OffsetTime.of(to, ZoneOffset.UTC));
        availableTime.setTimezone(ZoneOffset.UTC.getId());
        availableTime.setPersonId(personId);

        if (dayLimit != null) {
            DayLimit limit = new DayLimit();
            limit.setDayLimitId(new DayLimit.DayLimitId(personId, availableTime.getDate()));
            limit.setWalkCount(dayLimit);
            dayLimitRepository.save(limit);
            availableTime.setDayLimit(limit);
        }

        Location location = new Location();
        location.setPoint(point);
        availableTime.setLocation(location);
        availableTimeService.addAvailableTime(availableTime, personId);

        return availableTime;
    }
}