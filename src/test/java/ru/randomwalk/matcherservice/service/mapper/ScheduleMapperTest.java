package ru.randomwalk.matcherservice.service.mapper;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.randomwalk.matcherservice.AbstractContainerTest;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.Location;
import ru.randomwalk.matcherservice.service.AppointmentDetailsService;
import ru.randomwalk.matcherservice.service.AvailableTimeService;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class ScheduleMapperTest extends AbstractContainerTest {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private PersonService personService;

    @Autowired
    private AvailableTimeService availableTimeService;

    @Autowired
    private AppointmentDetailsService appointmentDetailsService;

    @Transactional
    @Rollback
    @Test
    void checkScheduleElementsCount() {
        UUID personId = UUID.randomUUID();
        UUID secondPersonId = UUID.randomUUID();
        personService.addNewPerson(new RegisteredUserInfoEvent(personId, "initial"));
        personService.addNewPerson(new RegisteredUserInfoEvent(secondPersonId, "initial"));
        addAvailableTimeForPerson(personId, GeometryUtil.createPoint(1,2), LocalTime.now().minusHours(1), LocalTime.now().plusHours(2), LocalDate.now().plusDays(1));
        addAvailableTimeForPerson(personId, GeometryUtil.createPoint(1,2), LocalTime.now().minusHours(1), LocalTime.now().plusHours(2), LocalDate.now().minusDays(1));
        addAvailableTimeForPerson(personId, GeometryUtil.createPoint(1,2), LocalTime.now().minusHours(1), LocalTime.now().plusHours(2), LocalDate.now());
        var pastAppointment = appointmentDetailsService.createAppointment(personId, secondPersonId, OffsetDateTime.now().minusDays(1), GeometryUtil.createPoint(1.0, 2.0));
        var currentAppointment = appointmentDetailsService.createAppointment(personId, secondPersonId, OffsetDateTime.now(), GeometryUtil.createPoint(1.0, 2.0));
        var futureAppointment = appointmentDetailsService.createAppointment(personId, secondPersonId, OffsetDateTime.now().plusDays(1), GeometryUtil.createPoint(1.0, 2.0));
        var doneAppointment = appointmentDetailsService.createAppointment(personId, secondPersonId, OffsetDateTime.now().plusDays(1), GeometryUtil.createPoint(1.0, 2.0));
        appointmentDetailsService.changeStatus(doneAppointment, AppointmentStatus.DONE);

        var result = scheduleMapper.getScheduleForPerson(personService.findById(personId));

        assertEquals(2, result.size());
        var currentDateSchedule = result.stream()
                .filter(dto -> dto.date().equals(currentAppointment.getStartDate()))
                .findFirst()
                .get();
        var futureSchedule = result.stream()
                .filter(dto -> dto.date().equals(futureAppointment.getStartDate()))
                .findFirst()
                .get();
        assertEquals(2, currentDateSchedule.timeFrames().size());
        assertEquals(2, futureSchedule.timeFrames().size());
    }

    private AvailableTime addAvailableTimeForPerson(UUID personId, Point point, LocalTime from, LocalTime to, LocalDate date) {
        AvailableTime availableTime = new AvailableTime();

        availableTime.setDate(date);
        availableTime.setTimeFrom(OffsetTime.of(from, ZoneOffset.UTC));
        availableTime.setTimeUntil(OffsetTime.of(to, ZoneOffset.UTC));
        availableTime.setTimezone(ZoneOffset.UTC.getId());
        availableTime.setPersonId(personId);

        Location location = new Location();
        location.setPoint(point);
        availableTime.setLocation(location);
        availableTimeService.addAvailableTime(availableTime, personId);

        return availableTime;
    }
}