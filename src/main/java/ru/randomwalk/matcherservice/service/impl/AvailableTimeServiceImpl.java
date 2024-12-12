package ru.randomwalk.matcherservice.service.impl;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.DayLimit;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.event.WalkOrganizerStartEvent;
import ru.randomwalk.matcherservice.model.model.AvailableTimeOverlapModel;
import ru.randomwalk.matcherservice.repository.AvailableTimeRepository;
import ru.randomwalk.matcherservice.repository.DayLimitRepository;
import ru.randomwalk.matcherservice.service.AvailableTimeService;
import ru.randomwalk.matcherservice.service.mapper.AvailableTimeMapper;
import ru.randomwalk.matcherservice.service.util.TimeUtil;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailableTimeServiceImpl implements AvailableTimeService {

    private final AvailableTimeRepository availableTimeRepository;
    private final AvailableTimeMapper availableTimeMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final DayLimitRepository dayLimitRepository;
    private final MatcherProperties matcherProperties;

    @Override
    public void addAvailableTime(List<AvailableTime> newAvailableTimes, UUID personId) {
        addDayLimitToAvailableTimes(newAvailableTimes);
        availableTimeRepository.saveAllAndFlush(newAvailableTimes);
        eventPublisher.publishEvent(new WalkOrganizerStartEvent(personId));
    }

    @Override
    @Transactional
    public void removeAvailableTimeForPerson(AvailableTime availableTime, Person person) {
        person.getAvailableTimes().remove(availableTime);
        availableTimeRepository.delete(availableTime);
    }

    @Override
    public List<AvailableTimeOverlapModel> getAllAvailableTimeOverlaps(List<AvailableTime> first, List<AvailableTime> second) {
        var firstDateToTimes = groupAvailableTimeByDate(first);
        var secondDateToTimes = groupAvailableTimeByDate(second);

        return firstDateToTimes.entrySet().stream()
                .filter(entry -> secondDateToTimes.containsKey(entry.getKey()))
                .flatMap(entry -> getStreamOfOverlappingTimeModels(entry.getValue(), secondDateToTimes.get(entry.getKey())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AvailableTime> splitAvailableTime(AvailableTime availableTime, OffsetTime splitFrom, OffsetTime splitUntil) {
        log.info("Splitting available time {} by interval (from: {}, until: {})", availableTime, splitFrom, splitUntil);
        List<AvailableTime> splitResult = new ArrayList<>();

        if (!availableTime.getTimeFrom().isEqual(splitFrom)) {
            AvailableTime beforeAvailableTime = availableTimeMapper.clone(availableTime);
            beforeAvailableTime.setTimeUntil(splitFrom);
            splitResult.add(beforeAvailableTime);
        }

        if (!availableTime.getTimeUntil().isEqual(splitUntil) && availableTime.getTimeFrom().isBefore(splitUntil)) {
            AvailableTime afterAvailableTime = availableTimeMapper.clone(availableTime);
            afterAvailableTime.setTimeFrom(splitUntil);
            splitResult.add(afterAvailableTime);
        }

        splitResult = availableTimeRepository.saveAll(splitResult);

        log.info("Split complete. Result: {}", splitResult);
        return splitResult;
    }

    @Override
    public void decrementDayLimit(DayLimit dayLimit) {
        dayLimit.decrementWalkCount();
        dayLimitRepository.save(dayLimit);
    }

    private Map<LocalDate, List<AvailableTime>> groupAvailableTimeByDate(List<AvailableTime> availableTimes) {
        return availableTimes.stream()
                .filter(this::isDayAvailableForWalk)
                .collect(Collectors.groupingBy(AvailableTime::getDate));
    }

    private boolean isDayAvailableForWalk(AvailableTime availableTime) {
        var dayLimit = availableTime.getDayLimit();
        return dayLimit.getWalkCount() != null && dayLimit.getWalkCount() > 0;
    }

    private Stream<AvailableTimeOverlapModel> getStreamOfOverlappingTimeModels(List<AvailableTime> firstTimes, List<AvailableTime> secondTimes) {
        return firstTimes.stream()
                .flatMap(time -> getStreamOfFoundOverlapModels(time, secondTimes));
    }

    private Stream<AvailableTimeOverlapModel> getStreamOfFoundOverlapModels(AvailableTime initialTime, List<AvailableTime> times) {
        return times.stream()
                .map(time -> getOverlapModel(initialTime, time))
                .filter(Objects::nonNull)
                .filter(this::isTimeDifferenceGreaterThanWalkTime);
    }

    @Nullable
    private AvailableTimeOverlapModel getOverlapModel(AvailableTime firstAvailableTime, AvailableTime secondAvailableTime) {
        Pair<OffsetTime, OffsetTime> overlap = TimeUtil.getOverlappingInterval(
                Pair.of(firstAvailableTime.getTimeFrom(), firstAvailableTime.getTimeUntil()),
                Pair.of(secondAvailableTime.getTimeFrom(), secondAvailableTime.getTimeUntil())
        );

        if (overlap == null) {
            return null;
        }

        return new AvailableTimeOverlapModel(
                firstAvailableTime.getDate(),
                overlap.getLeft(),
                overlap.getRight(),
                firstAvailableTime,
                secondAvailableTime
        );
    }

    private boolean isTimeDifferenceGreaterThanWalkTime(AvailableTimeOverlapModel overlapModel) {
        var overlapInterval = Pair.of(overlapModel.timeFrom(), overlapModel.timeUntil());
        return TimeUtil.isDifferenceWithinIntervalExist(overlapInterval, matcherProperties.getMinWalkTimeInSeconds());
    }

    private void addDayLimitToAvailableTimes(List<AvailableTime> availableTimes) {
        setMaximumWalkCountForNullLimit(availableTimes);
        List<DayLimit> dayLimits = availableTimes.stream()
                .map(AvailableTime::getDayLimit)
                .toList();

        dayLimitRepository.saveAll(dayLimits);
    }

    private void setMaximumWalkCountForNullLimit(List<AvailableTime> availableTimes) {
        Map<LocalDate, List<AvailableTime>> dateListMap = availableTimes.stream()
                .collect(Collectors.groupingBy(AvailableTime::getDate));

        for (var entry : dateListMap.entrySet()) {
            var dateAvailableTimes = entry.getValue();
            Integer maximumWalkCount = dateAvailableTimes.stream()
                    .map(time -> ChronoUnit.SECONDS.between(time.getTimeUntil(), time.getTimeFrom()))
                    .mapToInt(duration -> (int) (duration / matcherProperties.getMinWalkTimeInSeconds()))
                    .sum();

            dateAvailableTimes.stream()
                    .map(AvailableTime::getDayLimit)
                    .filter(dayLimit -> dayLimit.getWalkCount() == null)
                    .forEach(dayLimit -> dayLimit.setWalkCount(maximumWalkCount));
        }
    }
}
