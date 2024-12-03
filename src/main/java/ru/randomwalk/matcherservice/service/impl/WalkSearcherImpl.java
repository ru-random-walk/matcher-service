package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.model.enam.FilterType;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.event.WalkSearchStartEvent;
import ru.randomwalk.matcherservice.model.exception.MatcherBadRequestException;
import ru.randomwalk.matcherservice.model.model.AvailableTimeOverlapModel;
import ru.randomwalk.matcherservice.model.model.MatchingResultModel;
import ru.randomwalk.matcherservice.service.WalkSearcher;
import ru.randomwalk.matcherservice.service.AppointmentSchedulingService;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.filters.MatchingHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalkSearcherImpl implements WalkSearcher {

    private final PersonService personService;
    private final AppointmentSchedulingService appointmentSchedulingService;
    private final List<MatchingHandler> matchingHandlers;

    @Override
    @Async
    @EventListener(WalkSearchStartEvent.class)
    public void startWalkSearch(WalkSearchStartEvent event) {
        UUID personId = event.personId();
        log.info("Starting matching algorithm for person {}", personId);

        Person person = personService.findById(personId);
        List<MatchingResultModel> matchingResults = findPartnersForPerson(person);

        if (!matchingResults.isEmpty()) {
            scheduleAppointmentsForFoundMatches(person, matchingResults);
        }
        setInSearchIfNeeded(person);

        personService.save(person);
        log.info("Matching algorithm complete for person {}", personId);
    }

    private void scheduleAppointmentsForFoundMatches(Person person, List<MatchingResultModel> matchingResults) {
        var idToPartnerMap = getIdToPartnerMap(matchingResults);
        matchingResults.stream()
                .flatMap(result -> result.availableTimeOverlapModels().stream())
                .sorted(AvailableTimeOverlapModel::compareTo)
                .forEach(model -> scheduleAppointment(person, model, idToPartnerMap));
    }

    private void scheduleAppointment(Person person, AvailableTimeOverlapModel overlapModel, Map<UUID, Person> idToPartnerMap) {
        UUID partnerId = overlapModel.selectedCandidateId();
        Person partner = idToPartnerMap.get(partnerId);

        appointmentSchedulingService.scheduleAppointmentWithOverlap(person, partner, overlapModel);
    }

    private Map<UUID, Person> getIdToPartnerMap(List<MatchingResultModel> matchingResults) {
        Map<UUID, Person> idToPartner = new HashMap<>();
        for (var match : matchingResults) {
            Person partner = match.matchedPartner();
            idToPartner.put(partner.getId(), partner);
        }
        return idToPartner;
    }

    private List<MatchingResultModel> findPartnersForPerson(Person person) {
        FilterType filterType = person.getGroupFilterType();
        return matchingHandlers.stream()
                .filter(handler -> handler.supports(filterType))
                .findAny()
                .map(handler -> handler.getMatches(person))
                .orElseThrow(() -> new MatcherBadRequestException("Couldn't find matcher for person " + person.getId()));
    }

    private void setInSearchIfNeeded(Person person) {
        if (person.getAvailableTimes() != null && !person.getAvailableTimes().isEmpty()) {
            log.info("Set person {} to in search status", person.getId());
            person.setInSearch(true);
        }
    }
}
