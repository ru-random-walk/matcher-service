package ru.randomwalk.matcherservice.service.impl;

import com.nimbusds.jose.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.randomwalk.matcherservice.config.MatcherProperties;
import ru.randomwalk.matcherservice.model.enam.FilterType;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.event.WalkSearchStartEvent;
import ru.randomwalk.matcherservice.model.exception.MatcherBadRequestException;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.WalkSearcher;
import ru.randomwalk.matcherservice.service.filters.MatchingHandler;

import java.util.List;
import java.util.UUID;

import static ru.randomwalk.matcherservice.service.util.TimeUtil.isDifferenceWithinIntervalExist;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalkSearcherImpl implements WalkSearcher {

    private final PersonService personService;
    private final List<MatchingHandler> matchingHandlers;
    private final MatcherProperties matcherProperties;

    @Override
    @Async
    @TransactionalEventListener(WalkSearchStartEvent.class)
    public void startWalkSearch(WalkSearchStartEvent event) {
        UUID personId = event.personId();
        log.info("Starting matching algorithm for person {}", personId);

        Person person = personService.findById(personId);
        List<AppointmentDetails> matchingResults = matchPerson(person);
        if (matchingResults.isEmpty()) {
            log.info("No walks were appointed for person {}", person.getId());
        }
        setInSearchIfNeeded(person);

        personService.save(person);
        log.info("Matching algorithm complete for person {}", personId);
    }

    private List<AppointmentDetails> matchPerson(Person person) {
        FilterType filterType = person.getGroupFilterType();
        return matchingHandlers.stream()
                .filter(handler -> handler.supports(filterType))
                .findAny()
                .map(handler -> handler.getMatches(person))
                .orElseThrow(() -> new MatcherBadRequestException("Couldn't find matcher for person " + person.getId()));
    }

    private void setInSearchIfNeeded(Person person) {
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
