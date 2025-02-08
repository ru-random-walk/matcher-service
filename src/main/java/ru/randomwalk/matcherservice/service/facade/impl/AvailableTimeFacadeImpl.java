package ru.randomwalk.matcherservice.service.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.model.dto.AvailableTimeCreateDto;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.service.AvailableTimeService;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.facade.AvailableTimeFacade;
import ru.randomwalk.matcherservice.service.mapper.AvailableTimeMapper;
import ru.randomwalk.matcherservice.service.validation.AppointmentValidator;

import java.security.Principal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailableTimeFacadeImpl implements AvailableTimeFacade {

    private final AppointmentValidator appointmentValidator;
    private final AvailableTimeMapper availableTimeMapper;
    private final AvailableTimeService availableTimeService;
    private final PersonService personService;

    @Override
    public void addAvailableTime(AvailableTimeCreateDto dto, Principal principal) {
        UUID personId = UUID.fromString(principal.getName());
        Person person = personService.findByIdWithFetchedAvailableTime(personId);
        appointmentValidator.validateCreateRequest(dto, person);
        AvailableTime newAvailableTime = availableTimeMapper.fromCreationDto(dto, personId);

        availableTimeService.addAvailableTime(newAvailableTime, personId);
    }
}
