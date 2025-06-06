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
import ru.random.walk.dto.CreatePrivateChatEvent;
import ru.random.walk.dto.SendNotificationEvent;
import ru.random.walk.topic.EventTopic;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.dto.AppointmentCreationResultDto;
import ru.randomwalk.matcherservice.model.dto.TimePeriod;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.service.AppointmentCreationService;
import ru.randomwalk.matcherservice.service.AvailableTimeService;
import ru.randomwalk.matcherservice.service.DayLimitService;
import ru.randomwalk.matcherservice.service.OutboxSenderService;
import ru.randomwalk.matcherservice.service.util.NotificationConstants;
import ru.randomwalk.matcherservice.service.util.TimeUtil;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class AppointmentManagementJob implements Job {

    public static final String PERSON_ID_JOB_KEY = "personId";
    public static final String AVAILABLE_TIME_ID_JOB_KEY = "availableTimeId";
    public static final String TRACE_ID_JOB_KEY = "traceId";

    private final AvailableTimeService availableTimeService;
    private final DayLimitService dayLimitService;
    private final MatcherProperties matcherProperties;
    private final AppointmentCreationService appointmentCreationService;
    private final OutboxSenderService outboxSenderService;

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

        var creationResults = createAppointmentsAndGetPartnerIds(availableTimeToMatch, matchingAvailableTimes);
        createChatsWithPartners(personId, getUniquePartners(creationResults));
        notifyPeopleAboutAppointment(personId, creationResults);
    }

    private List<AppointmentCreationResultDto> createAppointmentsAndGetPartnerIds(
            AvailableTime initialAvailableTime,
            List<AvailableTime> matchingAvailableTimes
    ) {
        Queue<AvailableTime> availableTimes = new ArrayDeque<>();
        availableTimes.add(initialAvailableTime);

        List<AppointmentCreationResultDto> results = new ArrayList<>();
        while (!availableTimes.isEmpty()) {
            var availableTime = availableTimes.poll();
            for (var matchingTime : matchingAvailableTimes) {
                if (!canOrganizeWalk(availableTime) || !canOrganizeWalk(matchingTime)) {
                    break;
                }
                tryToCreateAppointment(availableTime, matchingTime)
                        .ifPresent(result -> {
                            availableTimes.addAll(result.initialAvailableTimeSplit());
                            results.add(result);
                        });
            }
        }
        log.info("{} appointments were scheduled for person {}", results.size(), initialAvailableTime.getPersonId());
        return results;
    }

    private Optional<AppointmentCreationResultDto> tryToCreateAppointment(AvailableTime availableTime, AvailableTime matchingTime) {
        try {
            var overlappingInterval = getOverlap(availableTime, matchingTime);
            if (!overlapForWalkExists(overlappingInterval)) {
                return Optional.empty();
            }

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

    private void createChatsWithPartners(UUID personId, Set<UUID> partners) {
        var events = partners.stream()
                .map(partner -> new CreatePrivateChatEvent(personId, partner))
                .toList();
        outboxSenderService.sendBatchOfMessages(EventTopic.CREATE_CHAT, events);
        log.info("{} chats were created", partners.size());
    }

    private void notifyPeopleAboutAppointment(UUID initialPersonId, List<AppointmentCreationResultDto> creationResults) {
        var resultsByPeople = getAppointmentResultsByPersonIdMap(initialPersonId, creationResults);
        var notifications = resultsByPeople.entrySet().stream()
                .map(this::getNotificationForPerson)
                .toList();
        outboxSenderService.sendBatchOfMessages(EventTopic.SEND_NOTIFICATION, notifications);
    }

    private Map<UUID, List<AppointmentCreationResultDto>> getAppointmentResultsByPersonIdMap(
            UUID initialPersonId,
            List<AppointmentCreationResultDto> creationResults
    ) {
        Map<UUID, List<AppointmentCreationResultDto>> resultsByPeople = creationResults.stream()
                .collect(Collectors.groupingBy(AppointmentCreationResultDto::partnerId));

        if (!creationResults.isEmpty()) {
            resultsByPeople.put(initialPersonId, creationResults);
        }

        return resultsByPeople;
    }

    private SendNotificationEvent getNotificationForPerson(Map.Entry<UUID, List<AppointmentCreationResultDto>> entry) {
        UUID personId = entry.getKey();
        List<AppointmentCreationResultDto> appointmentsForPerson = entry.getValue();
        if (appointmentsForPerson.size() > 1) {
            return getGroupedNotification(personId, appointmentsForPerson);
        } else {
            AppointmentCreationResultDto singleAppointment = appointmentsForPerson.getFirst();
            return getSingleNotification(personId, singleAppointment.appointmentDetails());
        }
    }

    private SendNotificationEvent getGroupedNotification(UUID personId, List<AppointmentCreationResultDto> appointments) {
        return new SendNotificationEvent(
                personId,
                String.format("%d новых прогулок было назначено!", appointments.size()),
                "Поспеши ознакомиться со своим расписанием!"
        );
    }

    private SendNotificationEvent getSingleNotification(UUID personId, AppointmentDetails appointmentDetails) {
        return new SendNotificationEvent(
                personId,
                "Новая прогулка была назначена!",
                String.format(
                        "Новая прогулка состоится в %s! Перейди по уведомлению, чтобы узнать детали!",
                        appointmentDetails.getStartsAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ),
                Map.of(NotificationConstants.APPOINTMENT_ARG_NAME, appointmentDetails.getId().toString())
        );
    }

    private Set<UUID> getUniquePartners(List<AppointmentCreationResultDto> creationResults) {
        return creationResults.stream()
                .map(AppointmentCreationResultDto::partnerId)
                .collect(Collectors.toSet());
    }

    private TimePeriod getOverlap(AvailableTime first, AvailableTime second) {
        return TimeUtil.getOverlappingInterval(
                TimePeriod.of(first.getTimeFrom(), first.getTimeUntil()),
                TimePeriod.of(second.getTimeFrom(), second.getTimeUntil())
        );
    }

    private boolean overlapForWalkExists(TimePeriod overlap) {
        return overlap != null
                && ChronoUnit.SECONDS.between(overlap.from(), overlap.until()) >= matcherProperties.getMinWalkTimeInSeconds();
    }

    private boolean canOrganizeWalk(AvailableTime availableTime) {
        boolean dayLimitIsNotExceeded = dayLimitService.getCurrentWalkCountForAvailableTime(availableTime) > 0;
        boolean hasTime = ChronoUnit.SECONDS.between(availableTime.getTimeFrom(), availableTime.getTimeUntil()) >= matcherProperties.getMinWalkTimeInSeconds();

        return hasTime && dayLimitIsNotExceeded;
    }
}
