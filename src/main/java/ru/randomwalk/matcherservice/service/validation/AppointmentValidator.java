package ru.randomwalk.matcherservice.service.validation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.dto.AvailableTimeModifyDto;
import ru.randomwalk.matcherservice.model.dto.TimePeriod;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.Club;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.exception.MatcherBadRequestException;
import ru.randomwalk.matcherservice.model.exception.MatcherForbiddenException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.randomwalk.matcherservice.service.util.TimeUtil.getOverlappingInterval;
import static ru.randomwalk.matcherservice.service.util.TimeUtil.isAfterOrEqual;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentValidator {

    private final MatcherProperties matcherProperties;

    public void validateCreateRequest(AvailableTimeModifyDto requestDto, Person person) {
        checkThatTimeZoneIsTheSame(requestDto);
        checkThatTimeFramesAreCorrect(requestDto);
        checkThatClubsInFilterAreCorrect(requestDto.clubsInFilter(), person);
        checkThatDoesNotConflictWithExistingAvailableTimes(requestDto, person.getAvailableTimes());
        checkThatDoesNotConflictWithActiveAppointments(requestDto, person.getAppointments());
    }

    public void validateChangeRequest(AvailableTimeModifyDto requestDto, Person person, AvailableTime availableTimeToChange) {
        checkThatAvailableTimeIsAttachedToUser(availableTimeToChange, person);
        checkDateDoesNotChange(requestDto, availableTimeToChange);
        checkThatTimeZoneIsTheSame(requestDto);
        checkThatTimeFramesAreCorrect(requestDto);
        checkThatClubsInFilterAreCorrect(requestDto.clubsInFilter(), person);
        checkThatDoesNotConflictWithActiveAppointments(requestDto, person.getAppointments());
        checkThatDoesNotConflictWithExistingAvailableTimes(requestDto, getAvailableTimesWithoutSpecified(person, availableTimeToChange));
    }

    private void checkThatTimeZoneIsTheSame(AvailableTimeModifyDto dto) {
        boolean offsetIsSame = Objects.equals(dto.timeFrom().getOffset(), dto.timeUntil().getOffset());

        if (!offsetIsSame) {
            throw new MatcherBadRequestException("Time frame offset should be same for timeFrom and timeUntil");
        }
    }

    private void checkThatTimeFramesAreCorrect(AvailableTimeModifyDto createDto) {
        if (isImpossibleTimeFrame(createDto.timeFrom(), createDto.timeUntil())) {
            throw new MatcherBadRequestException(
                    "Impossible time frame [%s; %s]",
                    createDto.timeFrom(),
                    createDto.timeUntil()
            );
        }
    }

    private void checkThatClubsInFilterAreCorrect(List<UUID> clubsInFilter, Person person) {
        if (clubsInFilter == null) {
            return;
        }
        Set<UUID> personClubs = person.getClubs().stream().map(Club::getClubId).collect(Collectors.toSet());
        clubsInFilter.stream()
                .filter(personClubs::contains)
                .findAny()
                .ifPresent(clubId -> {
                    throw new MatcherBadRequestException("User %s does not have club %s", person.getId(), clubId);
                });
    }

    private void checkThatDoesNotConflictWithActiveAppointments(AvailableTimeModifyDto dto, List<AppointmentDetails> appointmentDetails) {
        Map<LocalDate, List<OffsetDateTime>> activeAppointmentTime = appointmentDetails.stream()
                .filter(detail -> detail.getStatus().isActive())
                .map(AppointmentDetails::getStartsAt)
                .collect(Collectors.groupingBy(OffsetDateTime::toLocalDate));

        List<OffsetDateTime> appointmentStarts = activeAppointmentTime.get(dto.date());
        if (appointmentStarts == null) {
            return;
        }

        for (var appointment : appointmentStarts) {
            if (conflictsWithAppointmentTime(dto.timeFrom(), dto.timeUntil(), appointment)) {
                throw new MatcherBadRequestException(
                        "Time frame %s [%s; %s] conflicts with existing appointment with start time: %s",
                        dto.date(),
                        dto.timeFrom(),
                        dto.timeUntil(),
                        appointment.atZoneSameInstant(dto.timeFrom().getOffset())
                );
            }
        }
    }

    private void checkThatDoesNotConflictWithExistingAvailableTimes(AvailableTimeModifyDto dto, List<AvailableTime> availableTimes) {
        var dateToAvailableTimeMap = getDateToAvailableTimeMap(availableTimes);
        List<AvailableTime> existingAvailableTimes = dateToAvailableTimeMap.get(dto.date());
        if (existingAvailableTimes == null || existingAvailableTimes.isEmpty()) {
            return;
        }

        for (var existingAvailableTime : existingAvailableTimes) {
            if (isConflictingTimeFrame(dto.timeFrom(), dto.timeUntil(), existingAvailableTime)) {
                throw new MatcherBadRequestException(
                        "Time frame %s [%s; %s] conflicts with existing available time %s [%s; %s]",
                        dto.date(),
                        dto.timeFrom(),
                        dto.timeUntil(),
                        existingAvailableTime.getDate(),
                        existingAvailableTime.getTimeFrom(),
                        existingAvailableTime.getTimeUntil()
                );
            }
        }
    }

    private void checkThatAvailableTimeIsAttachedToUser(AvailableTime availableTime, Person person) {
        if (!Objects.equals(availableTime.getPersonId(), person.getId())) {
            throw new MatcherForbiddenException();
        }
    }

    private void checkDateDoesNotChange(AvailableTimeModifyDto modifyDto, AvailableTime availableTime) {
        if (!Objects.equals(availableTime.getDate(), modifyDto.date())) {
            throw new MatcherBadRequestException("Date cannot be changed");
        }
    }

    private Map<LocalDate, List<AvailableTime>> getDateToAvailableTimeMap(List<AvailableTime> availableTimes) {
        Map<LocalDate, List<AvailableTime>> dateToAvailableTimeMap = new HashMap<>();
        for (var time : availableTimes) {
            dateToAvailableTimeMap.putIfAbsent(time.getDate(), new ArrayList<>());
            dateToAvailableTimeMap.get(time.getDate()).add(time);
        }
        return dateToAvailableTimeMap;
    }

    private boolean isConflictingTimeFrame(OffsetTime timeFrom, OffsetTime timeUntil, AvailableTime availableTime) {
        var overlap = getOverlappingInterval(
                TimePeriod.of(timeFrom, timeUntil),
                TimePeriod.of(availableTime.getTimeFrom(), availableTime.getTimeUntil())
        );

        return overlap != null;
    }

    private boolean conflictsWithAppointmentTime(OffsetTime timeFrom, OffsetTime timeUntil, OffsetDateTime appointmentTime) {
        var overlap = getOverlappingInterval(
                TimePeriod.of(timeFrom, timeUntil),
                TimePeriod.of(appointmentTime.toOffsetTime(), appointmentTime.toOffsetTime().plusSeconds(matcherProperties.getMinWalkTimeInSeconds()))
        );

        return overlap != null;
    }

    private boolean isImpossibleTimeFrame(OffsetTime timeFrom, OffsetTime timeUntil) {
        return isAfterOrEqual(timeFrom, timeUntil)
                || ChronoUnit.SECONDS.between(timeFrom, timeUntil) < matcherProperties.getMinWalkTimeInSeconds();
    }

    private List<AvailableTime> getAvailableTimesWithoutSpecified(Person person, AvailableTime timeToRemove) {
        List<AvailableTime> availableTimes = new ArrayList<>(person.getAvailableTimes());
        availableTimes.remove(timeToRemove);

        return availableTimes;
    }

}
