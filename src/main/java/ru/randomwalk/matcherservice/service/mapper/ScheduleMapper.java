package ru.randomwalk.matcherservice.service.mapper;

import brave.internal.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.dto.LocationDto;
import ru.randomwalk.matcherservice.model.dto.ScheduleTimeFrameDto;
import ru.randomwalk.matcherservice.model.dto.UserScheduleDto;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.entity.projection.AppointmentPartner;
import ru.randomwalk.matcherservice.service.AppointmentDetailsService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleMapper {

    private final MatcherProperties matcherProperties;
    private final AppointmentDetailsService appointmentDetailsService;
    private final AvailableTimeMapper availableTimeMapper;

    public List<UserScheduleDto> getScheduleForPerson(Person person) {
        var availableTimes = person.getAvailableTimes();
        var appointments = appointmentDetailsService.getAllNotPastAppointmentsForPersonSchedule(person.getId());

        var availableTimeByDate = availableTimes.stream()
                .filter(time -> time.getDate().isAfter(LocalDate.now()) || time.getDate().equals(LocalDate.now()))
                .collect(Collectors.groupingBy(AvailableTime::getDate));

        var appointmentByDate = appointments.stream()
                .collect(Collectors.groupingBy(AppointmentDetails::getStartDate));

        Set<LocalDate> scheduledDates = getScheduledDates(availableTimeByDate, appointmentByDate);
        Map<UUID, UUID> appointmentIdPartnerMap = getAppointmentToPartnerMap(person.getId(), appointments);
        return scheduledDates.stream()
                .map(date ->
                        buildCurrentDateScheduleDto(
                                availableTimeByDate.get(date),
                                appointmentByDate.get(date),
                                appointmentIdPartnerMap,
                                date
                        )
                )
                .collect(Collectors.toList());
    }

    private Map<UUID, UUID> getAppointmentToPartnerMap(UUID personId, List<AppointmentDetails> appointmentDetails) {
        return appointmentDetailsService.getAllPartnerIdsForPersonAppointments(personId, appointmentDetails)
                .stream()
                .collect(Collectors.toMap(AppointmentPartner::getAppointmentId, AppointmentPartner::getPartnerId));
    }

    private UserScheduleDto buildCurrentDateScheduleDto(
            List<AvailableTime> availableTimes,
            List<AppointmentDetails> appointments,
            Map<UUID, UUID> appointmentToPartnerMap,
            LocalDate date
    ) {
        var firstAvailableTime = isEmpty(availableTimes) ? null : availableTimes.getFirst();
        var firstAppointment = isEmpty(appointments) ? null : appointments.getFirst();

        Integer walkCount = getWalkCountFromFirstAvailableTime(firstAvailableTime);
        String timeZone = getTimeZone(firstAvailableTime, firstAppointment);
        List<ScheduleTimeFrameDto> timeFrames = Lists.concat(
                getTimeFrameFromAvailableTimes(availableTimes),
                getTimeFrameFromAppointments(appointments, appointmentToPartnerMap)
        );

        return UserScheduleDto.builder()
                .walkCount(walkCount)
                .timezone(timeZone)
                .date(date)
                .timeFrames(timeFrames)
                .build();
    }

    private List<ScheduleTimeFrameDto> getTimeFrameFromAvailableTimes(List<AvailableTime> availableTimes) {
        if (isEmpty(availableTimes)) {
            return Collections.emptyList();
        }

        return availableTimes.stream()
                .map(this::getTimeFrameFromAvailableTime)
                .collect(Collectors.toList());
    }

    private List<ScheduleTimeFrameDto> getTimeFrameFromAppointments(
            List<AppointmentDetails> appointments,
            Map<UUID, UUID> appointmentToPartnerMap
    ) {
        if (isEmpty(appointments)) {
            return Collections.emptyList();
        }

        return appointments.stream()
                .map(appointment -> getTimeFrameFromAppointment(appointment, appointmentToPartnerMap))
                .collect(Collectors.toList());
    }

    private ScheduleTimeFrameDto getTimeFrameFromAvailableTime(AvailableTime time) {
        return ScheduleTimeFrameDto.builder()
                .availableTimeId(time.getId())
                .timeFrom(time.getTimeFrom())
                .timeUntil(time.getTimeUntil())
                .availableTimeClubsInFilter(new ArrayList<>(time.getClubsInFilter()))
                .location(availableTimeMapper.toLocationDto(time.getLocation()))
                .build();
    }

    private ScheduleTimeFrameDto getTimeFrameFromAppointment(
            AppointmentDetails appointment,
            Map<UUID, UUID> appointmentToPartnerMap
    ) {
        return ScheduleTimeFrameDto.builder()
                .appointmentStatus(appointment.getStatus())
                .appointmentId(appointment.getId())
                .timeFrom(appointment.getStartsAt().toOffsetTime())
                .timeUntil(appointment.getStartsAt().toOffsetTime().plusSeconds(matcherProperties.getMinWalkTimeInSeconds()))
                .partnerId(appointmentToPartnerMap.get(appointment.getId()))
                .requesterId(appointment.getRequesterId())
                .location(
                        LocationDto.builder()
                                .longitude(appointment.getLongitude())
                                .latitude(appointment.getLatitude())
                                .build()
                )
                .build();
    }

    private Set<LocalDate> getScheduledDates(
            Map<LocalDate, List<AvailableTime>> timeByDate,
            Map<LocalDate, List<AppointmentDetails>> appointmentsByDate
    ) {
        Set<LocalDate> dates = new HashSet<>();
        dates.addAll(timeByDate.keySet());
        dates.addAll(appointmentsByDate.keySet());
        return dates;
    }

    private Integer getWalkCountFromFirstAvailableTime(AvailableTime firstAvailableTime) {
        return Optional.ofNullable(firstAvailableTime)
                .map(availableTime -> availableTime.getDayLimit().getWalkCount())
                .orElse(0);
    }

    private String getTimeZone(AvailableTime firstAvailableTime, AppointmentDetails firstAppointment) {
        return Optional.ofNullable(firstAvailableTime)
                .map(AvailableTime::getTimezone)
                .orElseGet(() -> firstAppointment.getTimezone());
    }
}
