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
import ru.randomwalk.matcherservice.AbstractPostgresContainerTest;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.service.AppointmentDetailsService;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static ru.randomwalk.matcherservice.model.enam.AppointmentStatus.DONE;
import static ru.randomwalk.matcherservice.model.enam.AppointmentStatus.IN_PROGRESS;

@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(MockitoExtension.class)
class AppointmentStatusTransitionJobTest extends AbstractPostgresContainerTest {


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
        personService.addNewPerson(RegisteredUserInfoEvent.builder().id(firstPersonId).build());
        personService.addNewPerson(RegisteredUserInfoEvent.builder().id(secondPersonId).build());
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
    void execute_TransferStatusToInProgress() throws SchedulerException {
        UUID firstPersonId = UUID.randomUUID();
        UUID secondPersonId = UUID.randomUUID();
        personService.addNewPerson(RegisteredUserInfoEvent.builder().id(firstPersonId).build());
        personService.addNewPerson(RegisteredUserInfoEvent.builder().id(secondPersonId).build());
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
        personService.addNewPerson(RegisteredUserInfoEvent.builder().id(firstPersonId).build());
        personService.addNewPerson(RegisteredUserInfoEvent.builder().id(secondPersonId).build());
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