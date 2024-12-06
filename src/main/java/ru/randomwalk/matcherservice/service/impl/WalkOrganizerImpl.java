package ru.randomwalk.matcherservice.service.impl;

import com.nimbusds.jose.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.event.WalkOrganizerStartEvent;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.WalkOrganizer;
import ru.randomwalk.matcherservice.service.filters.PartnerMatchingManager;

import java.util.List;
import java.util.UUID;

import static ru.randomwalk.matcherservice.service.util.TimeUtil.isDifferenceWithinIntervalExist;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalkOrganizerImpl implements WalkOrganizer {

    private final PersonService personService;
    private final PartnerMatchingManager partnerMatchingManager;
    private final MatcherProperties matcherProperties;

    @Override
    @Async
    @EventListener(WalkOrganizerStartEvent.class)
    public void organizeWalk(WalkOrganizerStartEvent event) {
        UUID personId = event.personId();
        log.info("Starting matching algorithm for person {}", personId);

        Person person = personService.findByIdWithFetchedAvailableTime(personId);
        setInSearchIfPossible(person);
        personService.saveAndFlush(person);

        List<Person> partners = partnerMatchingManager.findPartnersAndScheduleAppointment(person);

        partners.forEach(this::setInSearchIfPossible);
        personService.saveAll(partners);

        log.info("Matching algorithm complete for person {}", personId);
    }

    private void setInSearchIfPossible(Person person) {
        if (hasTimeForWalk(person)) {
            log.info("Set person {} to in search status", person.getId());
            person.setInSearch(true);
        }
    }

    private boolean hasTimeForWalk(Person person) {
        if (person.getAvailableTimes() == null || person.getAvailableTimes().isEmpty()) {
            return false;
        }

        return person.getAvailableTimes().stream()
                .map(time -> Pair.of(time.getTimeFrom(), time.getTimeUntil()))
                .anyMatch(interval -> isDifferenceWithinIntervalExist(interval, matcherProperties.getMinWalkTimeInSeconds()));
    }
}
