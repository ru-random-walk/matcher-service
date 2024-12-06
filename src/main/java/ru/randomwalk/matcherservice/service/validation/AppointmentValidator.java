package ru.randomwalk.matcherservice.service.validation;


import com.nimbusds.jose.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.dto.request.AppointmentRequestDto;
import ru.randomwalk.matcherservice.model.dto.request.AvailableTimeRequestDto;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.exception.MatcherBadRequestException;
import ru.randomwalk.matcherservice.service.util.TimeUtil;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.randomwalk.matcherservice.service.util.TimeUtil.getOverlappingInterval;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentValidator {

    private final MatcherProperties matcherProperties;

    public void validateCreateRequest(AppointmentRequestDto requestDto, Person person) {
        checkThatTimeFramesAreCorrect(requestDto.availableTime());
        checkMaximumDayLimit(requestDto.availableTime());
        checkThatDoesNotConflictWithExistingAvailableTimes(requestDto.availableTime(), person.getAvailableTimes());
    }

    private void checkThatTimeFramesAreCorrect(List<AvailableTimeRequestDto> availableTimes) {
        availableTimes.stream()
                .flatMap(request -> request.timeFrames().stream())
                .filter(this::isImpossibleTimeFrame)
                .findAny()
                .ifPresent(timeFrame -> {
                    throw new MatcherBadRequestException(
                            "Impossible time frame [%s; %s]",
                            timeFrame.timeFrom(),
                            timeFrame.timeUntil()
                    );
                });
    }

    private void checkThatDoesNotConflictWithExistingAvailableTimes(List<AvailableTimeRequestDto> requestDtos, List<AvailableTime> availableTimes) {
        var dateToAvailableTimeMap = getDateToAvailableTimeMap(availableTimes);
        for (var dto : requestDtos) {
            List<AvailableTime> existingAvailableTimes = dateToAvailableTimeMap.get(dto.date());
            if (existingAvailableTimes == null || existingAvailableTimes.isEmpty()) {
                continue;
            }

            for (var timeFrame : dto.timeFrames()) {
                for (var existingAvailableTime : existingAvailableTimes) {
                    if (isConflictingTimeFrame(timeFrame, existingAvailableTime)) {
                        throw new MatcherBadRequestException(
                                "Time frame %s [%s; %s] conflicts with existing available time %s [%s; %s]",
                                dto.date(),
                                timeFrame.timeFrom(),
                                timeFrame.timeUntil(),
                                existingAvailableTime.getDate(),
                                existingAvailableTime.getTimeFrom(),
                                existingAvailableTime.getTimeUntil()
                        );
                    }
                }
            }

        }

    }

    private void checkMaximumDayLimit(List<AvailableTimeRequestDto> requestDtos) {
        for (var dto : requestDtos) {
            int maximumDayLimit = calculateMaximumDayLimit(dto.timeFrames());
            if (dto.walkCount() >= maximumDayLimit) {
                throw new MatcherBadRequestException(
                        "Inappropriate walkCount = %d. Maximum walk count for %s is %d",
                        dto.walkCount(),
                        dto.date().toString(),
                        maximumDayLimit
                );
            }
        }
    }

    private Integer calculateMaximumDayLimit(List<AvailableTimeRequestDto.TimeFrame> timeFrames) {
        return timeFrames.stream()
                .map(this::getTimeFrameDurationInSeconds)
                .mapToInt(walkDuration -> (int) (walkDuration / matcherProperties.getMinWalkTimeInSeconds()))
                .sum();
    }

    private Map<LocalDate, List<AvailableTime>> getDateToAvailableTimeMap(List<AvailableTime> availableTimes) {
        Map<LocalDate, List<AvailableTime>> dateToAvailableTimeMap = new HashMap<>();
        for (var time : availableTimes) {
            dateToAvailableTimeMap.putIfAbsent(time.getDate(), new ArrayList<>());
            dateToAvailableTimeMap.get(time.getDate()).add(time);
        }
        return dateToAvailableTimeMap;
    }

    private boolean isConflictingTimeFrame(AvailableTimeRequestDto.TimeFrame timeFrame, AvailableTime availableTime) {
        var overlap = getOverlappingInterval(
                Pair.of(timeFrame.timeFrom(), timeFrame.timeUntil()),
                Pair.of(availableTime.getTimeFrom(), availableTime.getTimeUntil())
        );

        return overlap == null;
    }

    private boolean isImpossibleTimeFrame(AvailableTimeRequestDto.TimeFrame timeFrame) {
        return TimeUtil.isAfterOrEqual(timeFrame.timeFrom(), timeFrame.timeUntil())
                && getTimeFrameDurationInSeconds(timeFrame) >= matcherProperties.getMinWalkTimeInSeconds();
    }

    private long getTimeFrameDurationInSeconds(AvailableTimeRequestDto.TimeFrame timeFrame) {
        return ChronoUnit.SECONDS.between(timeFrame.timeFrom(), timeFrame.timeUntil());
    }
}
