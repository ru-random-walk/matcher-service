package ru.randomwalk.matcherservice.service.facade;

import ru.randomwalk.matcherservice.model.dto.AvailableTimeModifyDto;

import java.security.Principal;
import java.util.UUID;

public interface AvailableTimeFacade {

    void addAvailableTime(AvailableTimeModifyDto dto, Principal principal);

    void changeExistingAvailableTime(UUID id, AvailableTimeModifyDto dto, Principal principal);

    void deleteAvailableTime(UUID id, Principal principal);
}
