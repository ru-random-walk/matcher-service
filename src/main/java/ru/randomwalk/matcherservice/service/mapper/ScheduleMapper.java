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
        var appointments = person.getAppointments();

        var availableTimeByDate = availableTimes.stream()
                .collect(Collectors.groupingBy(AvailableTime::getDate));

        var appointmentByDate = appointments.stream()
                .collect(Collectors.groupingBy(AppointmentDetails::getStartDate));

        Set<LocalDate> scheduledDates = new HashSet<>();
        scheduledDates.addAll(appointmentByDate.keySet());
        scheduledDates.addAll(availableTimeByDate.keySet());

        return scheduledDates.stream()
                .map(date ->
                        buildCurrentDateScheduleDto(
                                availableTimeByDate.get(date),
                                appointmentByDate.get(date),
                                getAppointmentToPartnerMap(person.getId(), appointments),
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

        Integer walkCount = firstAvailableTime == null ? 0 : firstAvailableTime.getDayLimit().getWalkCount();
        String timeZone = firstAvailableTime == null ? firstAppointment.getTimezone() : firstAvailableTime.getTimezone();
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
                .map(time ->
                        ScheduleTimeFrameDto.builder()
                                .availableTimeId(time.getId())
                                .timeFrom(time.getTimeFrom())
                                .timeUntil(time.getTimeUntil())
                                .availableTimeClubsInFilter(new ArrayList<>(time.getClubsInFilter()))
                                .location(availableTimeMapper.toLocationDto(time.getLocation()))
                                .build()
                ).collect(Collectors.toList());
    }

    private List<ScheduleTimeFrameDto> getTimeFrameFromAppointments(
            List<AppointmentDetails> appointments,
            Map<UUID, UUID> appointmentToPartnerMap
    ) {
        if (isEmpty(appointments)) {
            return Collections.emptyList();
        }

        return appointments.stream()
                .filter(it -> it.getStatus().isShowInSchedule())
                .map(appointment ->
                        ScheduleTimeFrameDto.builder()
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
                                .build()
                ).collect(Collectors.toList());
    }
}
