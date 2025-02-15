package ru.randomwalk.matcherservice.service.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.dto.AppointmentCreationResultDto;
import ru.randomwalk.matcherservice.model.dto.TimePeriod;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.service.AppointmentCreationService;
import ru.randomwalk.matcherservice.service.AvailableTimeService;
import ru.randomwalk.matcherservice.service.util.TimeUtil;

import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class AppointmentManagementJob implements Job {

    public static final String PERSON_ID_JOB_KEY = "personId";
    public static final String AVAILABLE_TIME_ID_JOB_KEY = "availableTimeId";
    public static final String TRACE_ID_JOB_KEY = "traceId";

    private final AvailableTimeService availableTimeService;
    private final MatcherProperties matcherProperties;
    private final AppointmentCreationService appointmentCreationService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        UUID personId = (UUID) context.getJobDetail().getJobDataMap().get(PERSON_ID_JOB_KEY);
        UUID availableTimeId = (UUID) context.getJobDetail().getJobDataMap().get(AVAILABLE_TIME_ID_JOB_KEY);
        String traceId = (String) context.getJobDetail().getJobDataMap().get(TRACE_ID_JOB_KEY);
        MDC.put("traceId", traceId);

        log.info("Starting matching algorithm for person {} with availableTime id: {}", personId, availableTimeId);

        AvailableTime availableTimeToMatch = availableTimeService.getById(availableTimeId);
        if (!canOrganizeWalk(availableTimeToMatch)) {
            log.info("Day limit exceeded for {} at {}", personId, availableTimeToMatch.getDate());
            return;
        }

        List<AvailableTime> matchingAvailableTimes = availableTimeService.findMatchesForAvailableTime(availableTimeToMatch);
        log.info("Found {} matching times for available time {}", matchingAvailableTimes.size(), availableTimeId);

        createAppointments(availableTimeToMatch, matchingAvailableTimes);
    }

    private void createAppointments(AvailableTime initialAvailableTime, List<AvailableTime> matchingAvailableTimes) {
        Queue<AvailableTime> availableTimes = new ArrayDeque<>();
        availableTimes.add(initialAvailableTime);

        List<AppointmentDetails> appointments = new ArrayList<>();
        while (!availableTimes.isEmpty()) {
            var availableTime = availableTimes.poll();
            for (var matchingTime : matchingAvailableTimes) {
                if (!canOrganizeWalk(availableTime)) {
                    break;
                }
                tryToCreateAppointment(availableTime, matchingTime)
                        .ifPresent(result -> {
                            availableTimes.addAll(result.initialAvailableTimeSplit());
                            appointments.add(result.appointmentDetails());
                        });
            }
        }

        log.info("{} appointments were scheduled for person {}", appointments.size(), initialAvailableTime.getPersonId());
    }

    private Optional<AppointmentCreationResultDto> tryToCreateAppointment(AvailableTime availableTime, AvailableTime matchingTime) {
        var overlappingInterval = getOverlap(availableTime, matchingTime);
        if (overlappingInterval == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(
                    appointmentCreationService.createAppointmentForAvailableTime(
                            availableTime,
                            matchingTime,
                            overlappingInterval.from()
                    )
            );
        } catch (Exception e) {
            log.error(
                    "Error creating appointment for availableTimes: {} and {}",
                    availableTime.getId(), matchingTime.getId(), e
            );
        }
        return Optional.empty();
    }

    private TimePeriod getOverlap(AvailableTime first, AvailableTime second) {
        return TimeUtil.getOverlappingInterval(
                TimePeriod.of(first.getTimeFrom(), first.getTimeUntil()),
                TimePeriod.of(second.getTimeFrom(), second.getTimeUntil())
        );
    }

    private boolean canOrganizeWalk(AvailableTime availableTime) {
        boolean dayLimitIsNotExceeded = availableTimeService.getCurrentWalkCountWithLock(availableTime) > 0;
        boolean hasTime = ChronoUnit.SECONDS.between(availableTime.getTimeFrom(), availableTime.getTimeUntil()) >= matcherProperties.getMinWalkTimeInSeconds();

        return hasTime && dayLimitIsNotExceeded;
    }
}
