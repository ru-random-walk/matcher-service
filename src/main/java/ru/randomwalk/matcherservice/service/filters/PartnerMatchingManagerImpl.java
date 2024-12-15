package ru.randomwalk.matcherservice.service.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.service.AppointmentSchedulingService;
import ru.randomwalk.matcherservice.service.PersonService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartnerMatchingManagerImpl implements PartnerMatchingManager {

    private final PersonService personService;
    private final AppointmentSchedulingService appointmentSchedulingService;

    @Override
    public List<UUID> findPartnersAndScheduleAppointment(Person person) {
        log.info("Searching for partners for person: {} with filterType: {}", person.getId(), person.getGroupFilterType());

        List<UUID> appointedPartners = personService.getSuitableCandidatesIdsForPerson(person).stream()
                .filter(candidateId -> appointmentSchedulingService.tryToScheduleAppointmentBetweenPeople(person.getId(), candidateId).isPresent())
                .collect(Collectors.toList());

        log.info("Found {} partners for user {}", appointedPartners.size(), person.getId());
        return appointedPartners;
    }
}
