package ru.randomwalk.matcherservice.service.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.model.dto.request.AppointmentRequestDto;
import ru.randomwalk.matcherservice.model.dto.response.AvailableTimeResponseDto;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.service.AvailableTimeService;
import ru.randomwalk.matcherservice.service.facade.AppointmentFacade;
import ru.randomwalk.matcherservice.service.mapper.AvailableTimeMapper;
import ru.randomwalk.matcherservice.service.validation.AppointmentValidator;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentFacadeImpl implements AppointmentFacade {

    private final AppointmentValidator appointmentValidator;
    private final AvailableTimeMapper availableTimeMapper;
    private final AvailableTimeService availableTimeService;

    @Override
    public void createAppointment(AppointmentRequestDto dto, Principal principal) {
        UUID personId = UUID.fromString(principal.getName());
        appointmentValidator.validate(dto);
        List<AvailableTime> availableTimes = availableTimeMapper.fromRequests(dto.availableTime(), personId);

        availableTimeService.addAvailableTime(availableTimes, personId);
    }
}
