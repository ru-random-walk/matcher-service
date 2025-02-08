package ru.randomwalk.matcherservice.service.facade;

import ru.randomwalk.matcherservice.model.dto.AvailableTimeCreateDto;

import java.security.Principal;

public interface AvailableTimeFacade {

    void addAvailableTime(AvailableTimeCreateDto dto, Principal principal);
}
