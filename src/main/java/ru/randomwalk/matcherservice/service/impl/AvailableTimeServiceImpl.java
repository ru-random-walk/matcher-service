package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.dto.AvailableTimeModifyDto;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.Club;
import ru.randomwalk.matcherservice.model.entity.DayLimit;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.exception.MatcherNotFoundException;
import ru.randomwalk.matcherservice.repository.AvailableTimeRepository;
import ru.randomwalk.matcherservice.repository.DayLimitRepository;
import ru.randomwalk.matcherservice.service.AvailableTimeService;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.job.AppointmentManagementJob;
import ru.randomwalk.matcherservice.service.mapper.AvailableTimeMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.randomwalk.matcherservice.service.job.AppointmentManagementJob.AVAILABLE_TIME_ID_JOB_KEY;
import static ru.randomwalk.matcherservice.service.job.AppointmentManagementJob.PERSON_ID_JOB_KEY;
import static ru.randomwalk.matcherservice.service.job.AppointmentManagementJob.TRACE_ID_JOB_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailableTimeServiceImpl implements AvailableTimeService {

    private final AvailableTimeRepository availableTimeRepository;
    private final AvailableTimeMapper availableTimeMapper;
    private final DayLimitRepository dayLimitRepository;
    private final MatcherProperties matcherProperties;
    private final PersonService personService;
    private final Scheduler scheduler;

    @Override
    @Transactional
    public void addAvailableTime(AvailableTime availableTimeToCreate, UUID personId) {
        linkDayLimitToAvailableTime(availableTimeToCreate);
        var createdAvailableTime = availableTimeRepository.saveAndFlush(availableTimeToCreate);
        log.info("AvailableTime {} were created", createdAvailableTime.getId());
        scheduleAppointmentManagementJob(createdAvailableTime);
    }

    @Override
    @Transactional
    public List<AvailableTime> splitAvailableTime(AvailableTime availableTime, OffsetTime splitFrom, OffsetTime splitUntil) {
        log.info("Splitting available time {} by interval (from: {}, until: {})", availableTime, splitFrom, splitUntil);
        List<AvailableTime> splitResult = new ArrayList<>();

        if (!availableTime.getTimeFrom().isEqual(splitFrom)) {
            AvailableTime beforeAvailableTime = availableTimeMapper.clone(availableTime);
            beforeAvailableTime.setTimeUntil(splitFrom);
            if (isAvailableTimeInDurationLimit(beforeAvailableTime)) {
                splitResult.add(beforeAvailableTime);
            }
        }

        if (!availableTime.getTimeUntil().isEqual(splitUntil) && availableTime.getTimeFrom().isBefore(splitUntil)) {
            AvailableTime afterAvailableTime = availableTimeMapper.clone(availableTime);
            afterAvailableTime.setTimeFrom(splitUntil);
            if (isAvailableTimeInDurationLimit(afterAvailableTime)) {
                splitResult.add(afterAvailableTime);
            }
        }

        splitResult = availableTimeRepository.saveAll(splitResult);
        availableTimeRepository.delete(availableTime);

        log.info("Split complete. Result: {}", splitResult);
        return splitResult;
    }

    private boolean isAvailableTimeInDurationLimit(AvailableTime availableTime) {
        long duration = ChronoUnit.SECONDS.between(availableTime.getTimeFrom(), availableTime.getTimeUntil());
        return duration >= matcherProperties.getMinWalkTimeInSeconds();
    }

    @Override
    public AvailableTime getById(UUID id) {
        return availableTimeRepository.findById(id)
                .orElseThrow(() -> new MatcherNotFoundException("Available time with id=%s does not exist", id));
    }

    @Override
    public List<AvailableTime> findMatchesForAvailableTime(AvailableTime availableTimeToFindMatches) {
        List<AvailableTime> matchingTimes = availableTimeRepository.findMatchingAvailableTimes(
                availableTimeToFindMatches.getPersonId(),
                availableTimeToFindMatches.getLocation(),
                availableTimeToFindMatches.getSearchAreaInMeters(),
                availableTimeToFindMatches.getDate(),
                availableTimeToFindMatches.getTimeFrom(),
                availableTimeToFindMatches.getTimeUntil()
        );

        Person initialPerson = personService.findById(availableTimeToFindMatches.getPersonId());
        Map<UUID, Person> personMap = getPersonByAvailableTimeMap(matchingTimes);

        return matchingTimes.stream()
                .filter(matchingTime -> filterGroups(availableTimeToFindMatches, matchingTime, personMap))
                .map(time -> Pair.of(time, personService.getClubsSimilarityBetweenPeople(initialPerson, personMap.get(time.getPersonId()))))
                .sorted(Comparator.comparingInt(Pair::getRight))
                .map(Pair::getLeft)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void replaceExistingAvailableTime(UUID id, AvailableTimeModifyDto modifyDto) {
        var availableTime = getById(id);
        availableTime = availableTimeMapper.replaceAvailableTime(availableTime, modifyDto);
        availableTimeRepository.save(availableTime);

        log.info("AvailableTime {} has been changed", availableTime.getId());

        scheduleAppointmentManagementJob(availableTime);
    }

    @Override
    public void deleteAvailableTime(AvailableTime availableTime) {
        availableTimeRepository.delete(availableTime);
        deleteAppointmentManagementJobIfExists(availableTime);
    }

    private Map<UUID, Person> getPersonByAvailableTimeMap(List<AvailableTime> availableTimes) {
        List<UUID> peopleIds = availableTimes.stream()
                .map(AvailableTime::getPersonId)
                .toList();

        Map<UUID, Person> personById = personService.findAllWithFetchedClubs(peopleIds)
                .stream()
                .collect(Collectors.toMap(Person::getId, Function.identity()));

        return availableTimes.stream()
                .collect(Collectors.toMap(AvailableTime::getId, time -> personById.get(time.getPersonId())));
    }

    private boolean filterGroups(AvailableTime availableTimeToFindMatches, AvailableTime matchingTime, Map<UUID, Person> personMap) {
        Set<UUID> groupsInFilter = availableTimeToFindMatches.getClubsInFilter();
        Set<UUID> matchingTimeGroupsInFilter = matchingTime.getClubsInFilter();

        if (!matchingTimeGroupsInFilter.isEmpty() && !groupsInFilter.isEmpty()) {
            return matchingTimeGroupsInFilter.stream().anyMatch(groupsInFilter::contains);
        }

        if (!groupsInFilter.isEmpty()) {
            return personMap.get(matchingTime.getPersonId()).getClubs().stream()
                    .map(Club::getClubId)
                    .anyMatch(groupsInFilter::contains);
        }

        return true;
    }

    private void linkDayLimitToAvailableTime(AvailableTime availableTime) {
        var dayLimit = getOrCreateDayLimit(availableTime.getPersonId(), availableTime.getDate());
        availableTime.setDayLimit(dayLimit);
    }


    private DayLimit getOrCreateDayLimit(UUID personId, LocalDate date) {
        Optional<DayLimit> optionalDayLimit = dayLimitRepository.findByIdWithLock(new DayLimit.DayLimitId(personId, date));

        if (optionalDayLimit.isEmpty()) {
            DayLimit dayLimit = new DayLimit();
            dayLimit.setDayLimitId(new DayLimit.DayLimitId(personId, date));
            return dayLimitRepository.save(dayLimit);
        }

        return optionalDayLimit.get();
    }


    private void scheduleAppointmentManagementJob(AvailableTime availableTime) {
        JobDetail jobDetail = getJobDetail(availableTime);
        log.info("Scheduling appointment management job: {}", jobDetail.getKey());
        Trigger trigger = createTrigger(jobDetail);
        try {
            if (scheduler.checkExists(jobDetail.getKey())) {
                log.info("Job {} already exists. Deleting it", jobDetail.getKey());
                scheduler.deleteJob(jobDetail.getKey());
            }
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Job {} is scheduled", jobDetail.getKey());
        } catch (SchedulerException e) {
            log.error("Error scheduling job {}", jobDetail.getKey(), e);
        }
    }

    private void deleteAppointmentManagementJobIfExists(AvailableTime availableTime) {
        JobKey jobKey = JobKey.jobKey(getJobName(availableTime));
        try {
            log.info("Trying to delete job {}", jobKey);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("Job {} was deleted", jobKey);
            }
        } catch (SchedulerException e) {
            log.error("Quartz exception on job {} deletion", jobKey, e);
        }
    }

    private JobDetail getJobDetail(AvailableTime availableTime) {
        String jobName = getJobName(availableTime);
        return JobBuilder
                .newJob(AppointmentManagementJob.class)
                .withIdentity(jobName)
                .usingJobData(getJobDataMap(availableTime))
                .build();
    }

    private Trigger createTrigger(JobDetail jobDetail) {
        Date startDate = Date.from(LocalDateTime.now().plusSeconds(matcherProperties.getAppointmentManagerDelaySeconds()).toInstant(ZoneOffset.UTC));
        return TriggerBuilder
                .newTrigger()
                .withIdentity(jobDetail.getKey().getName())
                .forJob(jobDetail.getKey())
                .startAt(startDate)
                .build();
    }

    private String getJobName(AvailableTime time) {
        return String.format("AppointmentManagementJob-%s-%s", time.getPersonId(), time.getId());
    }

    private JobDataMap getJobDataMap(AvailableTime availableTime) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put(AVAILABLE_TIME_ID_JOB_KEY, availableTime.getId());
        jobDataMap.put(PERSON_ID_JOB_KEY, availableTime.getPersonId());
        jobDataMap.put(TRACE_ID_JOB_KEY, MDC.get("traceId"));

        return jobDataMap;
    }
}
