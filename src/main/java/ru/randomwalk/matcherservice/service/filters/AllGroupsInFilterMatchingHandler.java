package ru.randomwalk.matcherservice.service.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.model.enam.FilterType;
import ru.randomwalk.matcherservice.model.entity.Club;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.model.AvailableTimeOverlapModel;
import ru.randomwalk.matcherservice.model.model.MatchingResultModel;
import ru.randomwalk.matcherservice.repository.PersonRepository;
import ru.randomwalk.matcherservice.service.AvailableTimeService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AllGroupsInFilterMatchingHandler implements MatchingHandler {

    private final PersonRepository personRepository;
    private final AvailableTimeService availableTimeService;

    @Override
    public boolean supports(FilterType filterType) {
        return filterType == FilterType.ALL_MATCH;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchingResultModel> getMatches(Person person) {
        List<UUID> clubsInFilterId = getClubsInFilterIds(person);

        log.info("Searching for partners for person {} with clubs that are all matching to: {}", person.getId(), clubsInFilterId);

        List<MatchingResultModel> partnersWithTimeOverlap = new ArrayList<>();
        personRepository.findByDistanceAndAllGroupIdsInFilter(
                person.getId(),
                person.getLocation().getPosition(),
                Double.valueOf(person.getSearchAreaInMeters()),
                clubsInFilterId,
                clubsInFilterId.size()
        ).forEach(candidate -> addToPartnerListIfPossible(person, candidate, partnersWithTimeOverlap));

        log.info("Found {} suitable partners for {}", partnersWithTimeOverlap.size(), person.getId());
        return partnersWithTimeOverlap;
    }

    private void addToPartnerListIfPossible(
            Person person,
            Person candidate,
            List<MatchingResultModel> partnersWithTimeOverlap
    ) {
        List<AvailableTimeOverlapModel> timeOverlaps = availableTimeService.getAllAvailableTimeOverlaps(
                person.getAvailableTimes(),
                candidate.getAvailableTimes()
        );

        if (!timeOverlaps.isEmpty()) {
            log.info("Person {} is added to candidates list for person {}", candidate.getId(), person.getId());
            timeOverlaps.sort(AvailableTimeOverlapModel::compareTo);
            partnersWithTimeOverlap.add(new MatchingResultModel(candidate, timeOverlaps));
        }
    }

    private List<UUID> getClubsInFilterIds(Person person) {
        return person.getClubs().stream()
                .filter(Club::isInFilter)
                .map(Club::getClubId)
                .collect(Collectors.toList());
    }
}
