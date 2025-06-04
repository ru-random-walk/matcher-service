package ru.randomwalk.matcherservice.service.job;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Point;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.randomwalk.matcherservice.AbstractContainerTest;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.DayLimit;
import ru.randomwalk.matcherservice.model.entity.Location;
import ru.randomwalk.matcherservice.repository.DayLimitRepository;
import ru.randomwalk.matcherservice.service.AvailableTimeService;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static ru.randomwalk.matcherservice.model.enam.AppointmentStatus.APPOINTED;

@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(MockitoExtension.class)
class AppointmentManagementJobTest extends AbstractContainerTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private AppointmentManagementJob job;

    @Autowired
    private DayLimitRepository dayLimitRepository;

    @Autowired
    private AvailableTimeService availableTimeService;

    private final static Point location1 = GeometryUtil.createPoint(-30.262667, 42.032974);
    private final static Point location2 = GeometryUtil.createPoint(-30.262437, 42.032919);

    private static Stream<Arguments> provideTimeSlotsForMatches() {
        return Stream.of(
                Arguments.of(LocalTime.of(8, 30), LocalTime.of(19, 0), LocalTime.of(18, 0), LocalTime.of(19, 0)),
                Arguments.of(LocalTime.of(8, 30), LocalTime.of(10, 0), LocalTime.of(7, 0), LocalTime.of(10, 0)),
                Arguments.of(LocalTime.of(8, 30), LocalTime.of(9, 30), LocalTime.of(8, 30), LocalTime.of(9, 30))
        );
    }

    private static Stream<Arguments> provideTimeSlotsWithDifferentFilters() {
        return Stream.of(
                Arguments.of(LocalTime.of(8, 30), LocalTime.of(19, 0), LocalTime.of(18, 0), LocalTime.of(19, 0), List.of(UUID.randomUUID()), List.of()),
                Arguments.of(LocalTime.of(8, 30), LocalTime.of(10, 0), LocalTime.of(7, 0), LocalTime.of(10, 0), List.of(), List.of(UUID.randomUUID())),
                Arguments.of(LocalTime.of(8, 30), LocalTime.of(9, 30), LocalTime.of(8, 30), LocalTime.of(9, 30), List.of(UUID.randomUUID(), UUID.randomUUID()), List.of(UUID.randomUUID()))
        );
    }

    private static Stream<Arguments> provideTimeSlotsForMultipleMatches() {
        return Stream.of(
                Arguments.of(LocalTime.of(8, 0), LocalTime.of(19, 0), LocalTime.of(17, 0), LocalTime.of(19, 0), 2),
                Arguments.of(LocalTime.of(13, 0), LocalTime.of(17, 0), LocalTime.of(14, 0), LocalTime.of(16, 0), 2),
                Arguments.of(LocalTime.of(8, 0), LocalTime.of(20, 0), LocalTime.of(8, 0), LocalTime.of(12, 0), 4)
        );
    }

    private static Stream<Arguments> provideTimeSlotsThatNotMatching() {
        return Stream.of(
                Arguments.of(LocalTime.of(8, 30), LocalTime.of(19, 0), LocalTime.of(19, 0), LocalTime.of(23, 0)),
                Arguments.of(LocalTime.of(18, 30), LocalTime.of(20, 0), LocalTime.of(7, 0), LocalTime.of(10, 0)),
                Arguments.of(LocalTime.of(8, 30), LocalTime.of(9, 30), LocalTime.of(8, 45), LocalTime.of(9, 30)),
                Arguments.of(LocalTime.of(8, 30), LocalTime.of(10, 30), LocalTime.of(9, 45), LocalTime.of(10, 45))
        );
    }

    @ParameterizedTest
    @Transactional
    @Rollback
    @MethodSource("provideTimeSlotsForMatches")
    void checkThatAppointmentCreated(
            LocalTime personFrom,
            LocalTime personUntil,
            LocalTime partnerFrom,
            LocalTime partnerUntil
    ) throws JobExecutionException {
        //given
        var personId = UUID.randomUUID();
        var partnerId = UUID.randomUUID();
        List<UUID> personClubs = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<UUID> partnersClubs = List.of(UUID.randomUUID(), personClubs.getFirst(), UUID.randomUUID());
        personService.addNewOrUpdateExistingPerson(new RegisteredUserInfoEvent(personId, "initial"));
        personService.addNewOrUpdateExistingPerson(new RegisteredUserInfoEvent(partnerId, "partner"));
        AvailableTime initialTime = addAvailableTimeForPerson(personId, location1, personFrom, personUntil, null, personClubs);
        addAvailableTimeForPerson(partnerId, location2, partnerFrom, partnerUntil, null, partnersClubs);
        var context = Mockito.mock(JobExecutionContext.class);
        when(context.getJobDetail()).thenReturn(getDetail(initialTime.getId(), personId));

        //when
        job.execute(context);

        //then
        var person = personService.findById(personId);
        var partner = personService.findById(partnerId);
        var appointments = person.getAppointments();
        assertNotEquals(0, appointments.size());
        assertNotEquals(0, partner.getAppointments().size());
        assertEquals(APPOINTED, appointments.getFirst().getStatus());
        assertEquals(appointments.getFirst().getId(), partner.getAppointments().getFirst().getId());
    }

    @ParameterizedTest
    @Transactional
    @Rollback
    @MethodSource("provideTimeSlotsForMultipleMatches")
    void checkThatAppointmentCreated_MultipleAppointments(
            LocalTime personFrom,
            LocalTime personUntil,
            LocalTime partnerFrom,
            LocalTime partnerUntil,
            Integer expectedCreatedAppointmentsCount
    ) throws JobExecutionException {
        //given
        var personId = UUID.randomUUID();
        var partnerId = UUID.randomUUID();
        personService.addNewOrUpdateExistingPerson(new RegisteredUserInfoEvent(personId, "initial"));
        personService.addNewOrUpdateExistingPerson(new RegisteredUserInfoEvent(partnerId, "partner"));
        AvailableTime initialTime = addAvailableTimeForPerson(personId, location1, personFrom, personUntil, 100, List.of());
        addAvailableTimeForPerson(partnerId, location2, partnerFrom, partnerUntil, 100, List.of());
        var context = Mockito.mock(JobExecutionContext.class);
        when(context.getJobDetail()).thenReturn(getDetail(initialTime.getId(), personId));

        //when
        job.execute(context);

        //then
        var person = personService.findById(personId);
        var partner = personService.findById(partnerId);
        assertEquals(expectedCreatedAppointmentsCount, person.getAppointments().size());
        assertEquals(expectedCreatedAppointmentsCount, partner.getAppointments().size());
    }

    @ParameterizedTest
    @Transactional
    @Rollback
    @MethodSource("provideTimeSlotsWithDifferentFilters")
    void checkThatAppointmentCannotBeCreated_GroupFiltersAreDifferent(
            LocalTime personFrom,
            LocalTime personUntil,
            LocalTime partnerFrom,
            LocalTime partnerUntil,
            List<UUID> personClubs,
            List<UUID> partnerClubs
    ) throws JobExecutionException {
        //given
        var personId = UUID.randomUUID();
        var partnerId = UUID.randomUUID();
        personService.addNewOrUpdateExistingPerson(new RegisteredUserInfoEvent(personId, "initial"));
        personService.addNewOrUpdateExistingPerson(new RegisteredUserInfoEvent(partnerId, "partner"));
        AvailableTime initialTime = addAvailableTimeForPerson(personId, location1, personFrom, personUntil, null, personClubs);
        addAvailableTimeForPerson(partnerId, location2, partnerFrom, partnerUntil, null, partnerClubs);
        var context = Mockito.mock(JobExecutionContext.class);
        when(context.getJobDetail()).thenReturn(getDetail(initialTime.getId(), personId));

        //when
        job.execute(context);

        //then
        var person = personService.findById(personId);
        var partner = personService.findById(partnerId);
        var appointments = person.getAppointments();
        assertEquals(0, appointments.size());
        assertEquals(0, partner.getAppointments().size());
    }

    @ParameterizedTest
    @Transactional
    @Rollback
    @MethodSource("provideTimeSlotsThatNotMatching")
    void checkThatAppointmentCannotBeCreated_AvailableTimesNotOverlap(
            LocalTime personFrom,
            LocalTime personUntil,
            LocalTime partnerFrom,
            LocalTime partnerUntil
    ) throws JobExecutionException {
        //given
        var personId = UUID.randomUUID();
        var partnerId = UUID.randomUUID();
        personService.addNewOrUpdateExistingPerson(new RegisteredUserInfoEvent(personId, "initial"));
        personService.addNewOrUpdateExistingPerson(new RegisteredUserInfoEvent(partnerId, "partner"));
        AvailableTime initialTime = addAvailableTimeForPerson(personId, location1, personFrom, personUntil, null, List.of());
        addAvailableTimeForPerson(partnerId, location2, partnerFrom, partnerUntil, null, List.of());
        var context = Mockito.mock(JobExecutionContext.class);
        when(context.getJobDetail()).thenReturn(getDetail(initialTime.getId(), personId));

        //when
        job.execute(context);

        //then
        var person = personService.findById(personId);
        var partner = personService.findById(partnerId);
        var appointments = person.getAppointments();
        assertEquals(0, appointments.size());
        assertEquals(0, partner.getAppointments().size());
    }

    private AvailableTime addAvailableTimeForPerson(UUID personId, Point point, LocalTime from, LocalTime to, Integer dayLimit, List<UUID> clubs) {
        AvailableTime availableTime = new AvailableTime();

        availableTime.setDate(LocalDate.now());
        availableTime.setTimeFrom(OffsetTime.of(from, ZoneOffset.UTC));
        availableTime.setTimeUntil(OffsetTime.of(to, ZoneOffset.UTC));
        availableTime.setTimezone(ZoneOffset.UTC.getId());
        availableTime.setPersonId(personId);
        availableTime.setClubsInFilter(new HashSet<>(clubs));

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

    private JobDetail getDetail(UUID availableTimeId, UUID personId) {
        return JobBuilder
                .newJob(AppointmentManagementJob.class)
                .withIdentity("jobName")
                .usingJobData(getJobDataMap(availableTimeId, personId))
                .build();
    }

    private JobDataMap getJobDataMap(UUID availableTimeId, UUID personId) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put(AppointmentManagementJob.PERSON_ID_JOB_KEY, personId);
        jobDataMap.put(AppointmentManagementJob.AVAILABLE_TIME_ID_JOB_KEY, availableTimeId);
        jobDataMap.put(AppointmentManagementJob.TRACE_ID_JOB_KEY, MDC.get("traceId"));

        return jobDataMap;
    }
}