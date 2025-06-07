package ru.randomwalk.matcherservice.service.job;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.randomwalk.matcherservice.AbstractContainerTest;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.service.AppointmentDetailsService;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static ru.randomwalk.matcherservice.model.enam.AppointmentStatus.APPOINTED;
import static ru.randomwalk.matcherservice.model.enam.AppointmentStatus.CANCELED;
import static ru.randomwalk.matcherservice.model.enam.AppointmentStatus.DONE;
import static ru.randomwalk.matcherservice.model.enam.AppointmentStatus.IN_PROGRESS;
import static ru.randomwalk.matcherservice.model.enam.AppointmentStatus.REQUESTED;

@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(MockitoExtension.class)
class AppointmentStatusTransitionJobTest extends AbstractContainerTest {


    @Autowired
    private AppointmentDetailsService appointmentDetailsService;

    @Autowired
    private PersonService personService;

    @Autowired
    private AppointmentStatusTransitionJob job;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private MatcherProperties matcherProperties;

    @Test
    @Transactional
    @Rollback
    void checkJobIsScheduledAfterAppointmentWasCreated() throws SchedulerException {
        UUID firstPersonId = UUID.randomUUID();
        UUID secondPersonId = UUID.randomUUID();
        personService.addNewOrUpdateExistingPerson(RegisteredUserInfoEvent.builder().id(firstPersonId).build());
        personService.addNewOrUpdateExistingPerson(RegisteredUserInfoEvent.builder().id(secondPersonId).build());

        var newAppointment = appointmentDetailsService.createAppointment(firstPersonId, secondPersonId, OffsetDateTime.now().minusDays(1), GeometryUtil.createPoint(1.0, 2.0));
        var inProgressJobKey = JobKey.jobKey(String.format("AppointmentStatusTransitionJob-%s-%s", newAppointment.getId(), IN_PROGRESS));
        var doneJobKey = JobKey.jobKey(String.format("AppointmentStatusTransitionJob-%s-%s", newAppointment.getId(), DONE));

        assertTrue(scheduler.checkExists(inProgressJobKey));
        assertTrue(scheduler.checkExists(doneJobKey));

        var inProgressTrigger = scheduler.getTriggersOfJob(inProgressJobKey);
        var doneTrigger = scheduler.getTrigger(TriggerKey.triggerKey(doneJobKey.getName()));
        assertEquals(Date.from(newAppointment.getStartsAt().atZoneSameInstant(ZoneOffset.UTC).toInstant()), inProgressTrigger.getFirst().getNextFireTime());
        assertEquals(Date.from(newAppointment.getStartsAt().plusSeconds(matcherProperties.getMinWalkTimeInSeconds()).atZoneSameInstant(ZoneOffset.UTC).toInstant()), doneTrigger.getNextFireTime());
    }

    @Test
    @Transactional
    @Rollback
    void checkNoJobIsScheduledAfterAppointmentWasRequested() throws SchedulerException {
        //given
        UUID requesterId = UUID.randomUUID();
        UUID secondPersonId = UUID.randomUUID();
        personService.addNewOrUpdateExistingPerson(RegisteredUserInfoEvent.builder().id(requesterId).build());
        personService.addNewOrUpdateExistingPerson(RegisteredUserInfoEvent.builder().id(secondPersonId).build());

        //when
        var newAppointment = appointmentDetailsService.requestForAppointment(requesterId, secondPersonId, OffsetDateTime.now().minusDays(1), GeometryUtil.createPoint(1.0, 2.0));

        //then
        var inProgressJobKey = JobKey.jobKey(String.format("AppointmentStatusTransitionJob-%s-%s", newAppointment.getId(), IN_PROGRESS));
        var doneJobKey = JobKey.jobKey(String.format("AppointmentStatusTransitionJob-%s-%s", newAppointment.getId(), DONE));
        assertFalse(scheduler.checkExists(inProgressJobKey));
        assertFalse(scheduler.checkExists(doneJobKey));
        assertEquals(REQUESTED, newAppointment.getStatus());
        assertEquals(2, newAppointment.getMembers().size());
        assertEquals(requesterId, newAppointment.getRequesterId());
    }

    @Test
    @Transactional
    @Rollback
    void checkRequestedAppointmentsAreDeletedWhenNewWalkIsAppointed() {
        //given
        UUID requesterId = UUID.randomUUID();
        UUID secondPersonId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();
        var time = OffsetDateTime.of(LocalDate.now(), LocalTime.of(12, 0), ZoneOffset.UTC);
        personService.addNewOrUpdateExistingPerson(RegisteredUserInfoEvent.builder().id(requesterId).build());
        personService.addNewOrUpdateExistingPerson(RegisteredUserInfoEvent.builder().id(secondPersonId).build());
        personService.addNewOrUpdateExistingPerson(RegisteredUserInfoEvent.builder().id(personId).build());
        var requestedAppointment = appointmentDetailsService.requestForAppointment(requesterId, secondPersonId, time, GeometryUtil.createPoint(1.0, 2.0));

        //when
        var newAppointment = appointmentDetailsService.createAppointment(personId, secondPersonId, time, GeometryUtil.createPoint(1.0, 2.0));

        //then
        var cancelledAppointment = appointmentDetailsService.getById(requestedAppointment.getId());
        assertEquals(APPOINTED, newAppointment.getStatus());
        assertEquals(CANCELED, cancelledAppointment.getStatus());
    }

    @Test
    @Transactional
    @Rollback
    void execute_TransferStatusToInProgress() throws SchedulerException {
        UUID firstPersonId = UUID.randomUUID();
        UUID secondPersonId = UUID.randomUUID();
        personService.addNewOrUpdateExistingPerson(RegisteredUserInfoEvent.builder().id(firstPersonId).build());
        personService.addNewOrUpdateExistingPerson(RegisteredUserInfoEvent.builder().id(secondPersonId).build());
        var newAppointment = appointmentDetailsService.createAppointment(firstPersonId, secondPersonId, OffsetDateTime.now().minusDays(1), GeometryUtil.createPoint(1.0, 2.0));
        var context = Mockito.mock(JobExecutionContext.class);

        when(context.getJobDetail()).thenReturn(getDetail(newAppointment, IN_PROGRESS));

        job.execute(context);

        assertEquals(IN_PROGRESS, newAppointment.getStatus());
    }

    @Test
    @Transactional
    @Rollback
    void execute_TransferStatusToDoneAndEndAppointment() throws SchedulerException {
        UUID firstPersonId = UUID.randomUUID();
        UUID secondPersonId = UUID.randomUUID();
        personService.addNewOrUpdateExistingPerson(RegisteredUserInfoEvent.builder().id(firstPersonId).build());
        personService.addNewOrUpdateExistingPerson(RegisteredUserInfoEvent.builder().id(secondPersonId).build());
        var newAppointment = appointmentDetailsService.createAppointment(firstPersonId, secondPersonId, OffsetDateTime.now().minusDays(1), GeometryUtil.createPoint(1.0, 2.0));
        var context = Mockito.mock(JobExecutionContext.class);

        when(context.getJobDetail()).thenReturn(getDetail(newAppointment, DONE));

        job.execute(context);

        assertEquals(DONE, newAppointment.getStatus());
        assertNotNull(newAppointment.getEndedAt());
    }

    private JobDetail getDetail(AppointmentDetails appointment, AppointmentStatus toStatus) {
        return JobBuilder
                .newJob(AppointmentStatusTransitionJob.class)
                .withIdentity("jobName")
                .usingJobData(getJobDataMap(appointment.getId(), toStatus))
                .build();
    }

    private JobDataMap getJobDataMap(UUID appointmentId, AppointmentStatus toStatus) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put(AppointmentStatusTransitionJob.APPOINTMENT_ID_KEY, appointmentId);
        jobDataMap.put(AppointmentStatusTransitionJob.TO_STATUS_KEY, toStatus);

        return jobDataMap;
    }
}