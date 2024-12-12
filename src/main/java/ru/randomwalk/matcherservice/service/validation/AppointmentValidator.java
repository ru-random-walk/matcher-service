package ru.randomwalk.matcherservice.service.validation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.dto.request.AppointmentRequestDto;
import ru.randomwalk.matcherservice.model.dto.request.AvailableTimeRequestDto;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.exception.MatcherBadRequestException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.randomwalk.matcherservice.service.util.TimeUtil.getOverlappingInterval;
import static ru.randomwalk.matcherservice.service.util.TimeUtil.isAfterOrEqual;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentValidator {

    private final MatcherProperties matcherProperties;

    public void validateCreateRequest(AppointmentRequestDto requestDto, Person person) {
        checkThatTimeZoneIsTheSame(requestDto.availableTime());
        checkThatTimeFramesAreCorrect(requestDto.availableTime());
        checkMaximumDayLimit(requestDto.availableTime());
        checkThatDoesNotConflictWithExistingAvailableTimes(requestDto.availableTime(), person.getAvailableTimes());
        checkThatDoesNotConflictWithActiveAppointments(requestDto.availableTime(), person.getAppointments());
    }

    private void checkThatTimeZoneIsTheSame(List<AvailableTimeRequestDto> availableTimes) {
        var offset = availableTimes.getFirst().timeFrames().getFirst().timeFrom().getOffset();
        boolean offsetIsSame = availableTimes.stream()
                .flatMap(time -> time.timeFrames().stream())
                .allMatch(timeFrame -> offset.equals(timeFrame.timeFrom().getOffset()) && offset.equals(timeFrame.timeUntil().getOffset()));

        if (!offsetIsSame) {
            throw new MatcherBadRequestException("Time frame offset should be same for all available times");
        }
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

    private void checkThatDoesNotConflictWithActiveAppointments(List<AvailableTimeRequestDto> requestDtos, List<AppointmentDetails> appointmentDetails) {
        Map<LocalDate, List<OffsetDateTime>> activeAppointmentTime = appointmentDetails.stream()
                .filter(detail -> detail.getStatus().isActive())
                .map(AppointmentDetails::getStartsAt)
                .collect(Collectors.groupingBy(OffsetDateTime::toLocalDate));

        for (var dto : requestDtos) {
            List<OffsetDateTime> appointmentStarts = activeAppointmentTime.get(dto.date());
            if (appointmentStarts == null) {
                continue;
            }

            for (var timeFrame : dto.timeFrames()) {
                for (var appointment : appointmentStarts) {
                    if (conflictsWithAppointmentTime(timeFrame, appointment)) {
                        throw new MatcherBadRequestException(
                                "Time frame %s [%s; %s] conflicts with existing appointment with start time: %s",
                                dto.date(),
                                timeFrame.timeFrom(),
                                timeFrame.timeUntil(),
                                appointment.atZoneSameInstant(timeFrame.timeFrom().getOffset())
                        );
                    }
                }
            }
        }
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
            if (dto.walkCount() == null) {
                continue;
            }

            int maximumDayLimit = calculateMaximumDayLimit(dto.timeFrames());
            if (dto.walkCount() > maximumDayLimit || dto.walkCount() <= 0) {
                throw new MatcherBadRequestException(
                        "Inappropriate walkCount = %d. Maximum walk count for %s is %d",
                        dto.walkCount(),
                        dto.date(),
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

        return overlap != null;
    }

    private boolean conflictsWithAppointmentTime(AvailableTimeRequestDto.TimeFrame timeFrame, OffsetDateTime appointmentTime) {
        var overlap = getOverlappingInterval(
                Pair.of(timeFrame.timeFrom(), timeFrame.timeUntil()),
                Pair.of(appointmentTime.toOffsetTime(), appointmentTime.toOffsetTime().plusSeconds(matcherProperties.getMinWalkTimeInSeconds()))
        );

        return overlap != null;
    }

    private boolean isImpossibleTimeFrame(AvailableTimeRequestDto.TimeFrame timeFrame) {
        return isAfterOrEqual(timeFrame.timeFrom(), timeFrame.timeUntil())
                || getTimeFrameDurationInSeconds(timeFrame) < matcherProperties.getMinWalkTimeInSeconds();
    }

    private long getTimeFrameDurationInSeconds(AvailableTimeRequestDto.TimeFrame timeFrame) {
        return ChronoUnit.SECONDS.between(timeFrame.timeFrom(), timeFrame.timeUntil());
    }
}
