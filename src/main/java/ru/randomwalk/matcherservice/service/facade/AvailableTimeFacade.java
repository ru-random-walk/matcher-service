package ru.randomwalk.matcherservice.service.facade;

import ru.randomwalk.matcherservice.model.dto.request.AppointmentRequestDto;

import java.security.Principal;

public interface AvailableTimeFacade {

    void addAvailableTime(AppointmentRequestDto dto, Principal principal);
}
