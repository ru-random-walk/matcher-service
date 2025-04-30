package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.dto.TimePeriod;
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
import ru.randomwalk.matcherservice.service.util.TimeUtil;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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

    private static final List<String> STATUSES_NAMES_TO_NOT_SHOW_IN_SCHEDULE = Arrays.stream(AppointmentStatus.values())
            .filter(status -> !status.isShowInSchedule())
            .map(AppointmentStatus::name)
            .toList();

    @Override
    @Transactional
    public AppointmentDetails createAppointment(UUID personId, UUID partnerId, OffsetDateTime startsAt, Point approximateLocation) {
        log.info("Create appointment for people with ids: {}, {}. Starts at: {}", personId, partnerId, startsAt);
        var appointmentDetails = createNewAppointment(
                AppointmentStatus.APPOINTED,
                startsAt,
                approximateLocation,
                personId,
                partnerId
        );
        log.info("Appointment {} is created", appointmentDetails.getId());

        return appointmentDetails;
    }

    @Override
    @Transactional
    public AppointmentDetails requestForAppointment(UUID requesterId, UUID partnerId, OffsetDateTime startAt, Point location) {
        log.info("Creating request for appointment from {} to {} at {}", requesterId, partnerId, startAt);
        var appointmentRequest = createNewAppointment(
                AppointmentStatus.REQUESTED,
                startAt,
                location,
                requesterId,
                partnerId
        );
        appointmentRequest.setRequesterId(requesterId);

        log.info("Appointment {} is requested", appointmentRequest.getId());

        return appointmentRequest;
    }

    private AppointmentDetails createNewAppointment(AppointmentStatus status, OffsetDateTime startsAt, Point location, UUID... membersIds) {
        AppointmentDetails appointmentDetails = new AppointmentDetails();
        appointmentDetails.setStartsAt(startsAt);
        appointmentDetails.setLocation(location);
        appointmentDetails = appointmentDetailsRepository.save(appointmentDetails);

        linkMembersToAppointment(appointmentDetails, membersIds);
        changeStatus(appointmentDetails, status);

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
        cancelPendingStatusTransitionJobs(appointmentId);
        appointmentDetailsRepository.delete(appointment);
    }

    @Override
    @Transactional
    public void changeStatus(UUID appointmentId, AppointmentStatus toStatus) {
        var appointment = getById(appointmentId);
        changeStatus(appointment, toStatus);
    }

    @Override
    public List<AppointmentDetails> getAllNotPastAppointmentsForPersonSchedule(UUID personId) {
        return appointmentDetailsRepository.getAllAppointmentsForPersonThatStartsAfterDateAndNotInStatuses(
                personId,
                LocalDate.now(),
                STATUSES_NAMES_TO_NOT_SHOW_IN_SCHEDULE
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void changeStatus(AppointmentDetails appointment, AppointmentStatus toStatus) {
        log.info("Changing status of appointment {} from {} to {}", appointment.getId(), appointment.getStatus(), toStatus);

        if (appointment.getStatus() != null && !appointment.getStatus().isAllowedTransition(toStatus)) {
            throw new MatcherBadRequestException("Transition to status %s is not possible from %s", toStatus, appointment.getStatus());
        }

        appointment.setStatus(toStatus);
        if (toStatus.isTerminal()) {
            appointment.setEndedAt(OffsetDateTime.now());
        }

        if (toStatus == AppointmentStatus.APPOINTED) {
            appointment.getMembers()
                    .forEach(person -> cancelAllRequestedAppointmentsOnTime(person, appointment.getStartsAt()));
            scheduleStatusTransitionJob(appointment.getId(), AppointmentStatus.IN_PROGRESS, appointment.getStartsAt());
            scheduleStatusTransitionJob(appointment.getId(), AppointmentStatus.DONE, calculateEndTime(appointment.getStartsAt()));
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
        appointmentDetails.getMembers().addAll(people);
        personService.saveAll(people);
    }

    private void cancelAllRequestedAppointmentsOnTime(Person person, OffsetDateTime startTime) {
        List<AppointmentDetails> requestedAppointments = person.getAppointments().stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.REQUESTED)
                .filter(appointment -> hasOverlapWithTime(appointment, startTime))
                .toList();

        requestedAppointments.forEach(appointment -> changeStatus(appointment, AppointmentStatus.CANCELED));
    }

    private boolean hasOverlapWithTime(AppointmentDetails appointment, OffsetDateTime otherTime) {
        if (!Objects.equals(appointment.getStartDate(), otherTime.toLocalDate())) {
            return false;
        }
        TimePeriod timePeriod = TimeUtil.getOverlappingInterval(
                getWalkTimePeriod(otherTime),
                getWalkTimePeriod(appointment.getStartsAt())
        );
        return !Objects.isNull(timePeriod);
    }

    private TimePeriod getWalkTimePeriod(OffsetDateTime startTime) {
        return new TimePeriod(
                startTime.toOffsetTime(),
                startTime.toOffsetTime().plusSeconds(matcherProperties.getMinWalkTimeInSeconds())
        );
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

    private void cancelPendingStatusTransitionJobs(UUID appointmentId) {
        cancelTransitionJobIfExists(getJobName(appointmentId, AppointmentStatus.APPOINTED));
        cancelTransitionJobIfExists(getJobName(appointmentId, AppointmentStatus.DONE));
    }

    private void cancelTransitionJobIfExists(String jobName) {
        try {
            var key = JobKey.jobKey(jobName);
            if (scheduler.checkExists(key)) {
                scheduler.deleteJob(key);
            }
        } catch (Exception e) {
            log.error("Error cancelling job {}", jobName, e);
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
