package ru.randomwalk.matcherservice.service.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.model.dto.AvailableTimeModifyDto;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.exception.MatcherNotFoundException;
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
    public void addAvailableTime(AvailableTimeModifyDto dto, Principal principal) {
        UUID personId = UUID.fromString(principal.getName());
        Person person = personService.findById(personId);
        appointmentValidator.validateCreateRequest(dto, person);
        AvailableTime newAvailableTime = availableTimeMapper.fromModifyDto(dto, personId);

        availableTimeService.addAvailableTime(newAvailableTime, personId);
    }

    @Override
    public void changeExistingAvailableTime(UUID id, AvailableTimeModifyDto dto, Principal principal) {
        UUID personId = UUID.fromString(principal.getName());
        Person person = personService.findById(personId);
        AvailableTime availableTimeToChange = availableTimeService.getById(id);
        appointmentValidator.validateChangeRequest(dto, person, availableTimeToChange);

        availableTimeService.replaceExistingAvailableTime(id, dto);
    }

    @Override
    public void deleteAvailableTime(UUID id, Principal principal) {
        UUID personId = UUID.fromString(principal.getName());
        Person person = personService.findById(personId);

        AvailableTime availableTimeToDelete = person.getAvailableTimes().stream()
                .filter(time -> time.getId().equals(id))
                .findAny()
                .orElseThrow(() -> new MatcherNotFoundException("Available time with id %s is not found", id));

        availableTimeService.deleteAvailableTime(availableTimeToDelete);
    }
}
