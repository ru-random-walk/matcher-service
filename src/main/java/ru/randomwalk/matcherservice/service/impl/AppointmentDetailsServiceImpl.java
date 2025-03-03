package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.entity.projection.AppointmentPartner;
import ru.randomwalk.matcherservice.model.exception.MatcherBadRequestException;
import ru.randomwalk.matcherservice.model.exception.MatcherNotFoundException;
import ru.randomwalk.matcherservice.repository.AppointmentDetailsRepository;
import ru.randomwalk.matcherservice.service.AppointmentDetailsService;
import ru.randomwalk.matcherservice.service.DayLimitService;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.job.AppointmentStatusTransitionJob;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentDetailsServiceImpl implements AppointmentDetailsService {

    private final AppointmentDetailsRepository appointmentDetailsRepository;
    private final DayLimitService dayLimitService;
    private final PersonService personService;
    private final Scheduler scheduler;
    private final MatcherProperties matcherProperties;

    @Override
    @Transactional
    public AppointmentDetails createAppointment(UUID personId, UUID partnerId, OffsetDateTime startsAt, Point approximateLocation) {
        log.info("Create appointment for people with ids: {}, {}. Starts at: {}", personId, partnerId, startsAt);
        AppointmentDetails appointmentDetails = new AppointmentDetails();
        appointmentDetails.setStatus(AppointmentStatus.APPOINTED);
        appointmentDetails.setStartsAt(startsAt);
        appointmentDetails.setLocation(approximateLocation);

        appointmentDetails = appointmentDetailsRepository.save(appointmentDetails);
        linkMembersToAppointment(appointmentDetails, personId, partnerId);

        log.info("Appointment {} created", appointmentDetails.getId());

        scheduleStatusTransitionJob(appointmentDetails.getId(), AppointmentStatus.IN_PROGRESS, appointmentDetails.getStartsAt());
        scheduleStatusTransitionJob(appointmentDetails.getId(), AppointmentStatus.DONE, calculateEndTime(appointmentDetails.getStartsAt()));

        return appointmentDetails;
    }

    @Override
    public List<AppointmentPartner> getAllPartnerIdsForPersonAppointments(UUID personId, List<AppointmentDetails> appointments) {
        List<UUID> appointmentIds = appointments.stream()
                .map(AppointmentDetails::getId)
                .toList();

        return appointmentDetailsRepository.getAllPartnerIdsForAppointmentsOfPerson(personId, appointmentIds);
    }

    @Override
    public List<UUID> getAppointmentParticipants(UUID appointmentId) {
        return appointmentDetailsRepository.getAppointmentPartnerIds(appointmentId);
    }

    @Override
    public AppointmentDetails getById(UUID appointmentId) {
        return appointmentDetailsRepository.findById(appointmentId)
                .orElseThrow(() -> new MatcherNotFoundException("Appointment with id %s does not exist", appointmentId));
    }

    @Transactional
    @Override
    public void cancelAppointmentByPerson(UUID appointmentId, UUID initiatorId) {
        AppointmentDetails appointment = getById(appointmentId);
        List<UUID> participantsIds = getAppointmentParticipants(appointmentId);
        restoreDayLimitForPartner(appointment, initiatorId, participantsIds);
        unlinkAppointmentFromUsers(appointment, participantsIds);
        appointmentDetailsRepository.delete(appointment);
    }

    @Override
    @Transactional
    public void changeStatus(UUID appointmentId, AppointmentStatus toStatus) {
        var appointment = getById(appointmentId);
        changeStatus(appointment, toStatus);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void changeStatus(AppointmentDetails appointment, AppointmentStatus toStatus) {
        log.info("Changing status of appointment {} from {} to {}", appointment.getId(), appointment.getStatus(), toStatus);

        if (!appointment.getStatus().isAllowedTransition(toStatus)) {
            throw new MatcherBadRequestException("Transition to status %s is not possible from %s", toStatus, appointment.getStatus());
        }

        appointment.setStatus(toStatus);
        if (!toStatus.isActive()) {
            appointment.setEndedAt(OffsetDateTime.now());
        }

        log.info("Appointment {} status changed to {}", appointment.getId(), toStatus);
        appointmentDetailsRepository.save(appointment);
    }

    private void restoreDayLimitForPartner(AppointmentDetails appointmentDetails, UUID initiatorId, List<UUID> participants) {
        participants.stream()
                .filter(id -> !id.equals(initiatorId))
                .findFirst()
                .ifPresent(partnerId -> {
                    LocalDate date = appointmentDetails.getStartDate();
                    log.info("Restoring day limit for person {} at {}", partnerId, date);
                    dayLimitService.incrementDayLimitForPersonAndDate(partnerId, date);
                });
    }

    private void unlinkAppointmentFromUsers(AppointmentDetails appointment, List<UUID> participantIds) {
        List<Person> participants = personService.findAllWithFetchedAppointments(participantIds);
        participants.forEach(participant -> participant.getAppointments().remove(appointment));
        personService.saveAll(participants);
    }

    private void linkMembersToAppointment(AppointmentDetails appointmentDetails, UUID... membersIds) {
        List<Person> people = personService.findAllByIds(List.of(membersIds));
        people.forEach(person -> person.getAppointments().add(appointmentDetails));
        personService.saveAll(people);
    }

    private void scheduleStatusTransitionJob(UUID appointmentId, AppointmentStatus toStatus, OffsetDateTime jobFireTime) {
        var jobDetail = getStatusTransitionJobDetail(appointmentId, toStatus);
        var trigger = createTrigger(jobDetail, jobFireTime);

        log.info("Scheduling {} job", jobDetail.getKey());
        try {
            if (scheduler.checkExists(jobDetail.getKey())) {
                log.info("Transition to the same status already exists, rescheduling job");
                scheduler.rescheduleJob(trigger.getKey(), trigger);
                return;
            }
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("Error scheduling job {}", jobDetail.getKey(), e);
        }
    }

    private JobDetail getStatusTransitionJobDetail(UUID appointmentId, AppointmentStatus toStatus) {
        String jobName = getJobName(appointmentId, toStatus);
        return JobBuilder
                .newJob(AppointmentStatusTransitionJob.class)
                .withIdentity(jobName)
                .usingJobData(getJobDataMap(appointmentId, toStatus))
                .build();
    }

    private Trigger createTrigger(JobDetail jobDetail, OffsetDateTime jobFireTime) {
        Date startDate = Date.from(jobFireTime.atZoneSameInstant(ZoneOffset.UTC).toInstant());
        return TriggerBuilder
                .newTrigger()
                .withIdentity(jobDetail.getKey().getName())
                .forJob(jobDetail.getKey())
                .startAt(startDate)
                .build();
    }

    private String getJobName(UUID appointmentId, AppointmentStatus toStatus) {
        return String.format("AppointmentStatusTransitionJob-%s-%s", appointmentId, toStatus);
    }

    private JobDataMap getJobDataMap(UUID appointmentId, AppointmentStatus toStatus) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put(AppointmentStatusTransitionJob.APPOINTMENT_ID_KEY, appointmentId);
        jobDataMap.put(AppointmentStatusTransitionJob.TO_STATUS_KEY, toStatus);

        return jobDataMap;
    }

    private OffsetDateTime calculateEndTime(OffsetDateTime appointmentStartTime) {
        return appointmentStartTime
                .plusSeconds(matcherProperties.getMinWalkTimeInSeconds());
    }
}
