package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
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

        Person person = personService.findById(personId);
        setInSearchIfPossible(person);
        personService.saveAndFlush(person);

        List<UUID> partnersIds = partnerMatchingManager.findPartnersAndScheduleAppointment(person);

        List<Person> partners = personService.findAllByIds(partnersIds);
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
                .filter(availableTime -> availableTime.getDayLimit().getWalkCount() > 0)
                .map(time -> Pair.of(time.getTimeFrom(), time.getTimeUntil()))
                .anyMatch(interval -> isDifferenceWithinIntervalExist(interval, matcherProperties.getMinWalkTimeInSeconds()));
    }
}
