package ru.randomwalk.matcherservice.service.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.model.enam.FilterType;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.Club;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.model.AvailableTimeOverlapModel;
import ru.randomwalk.matcherservice.model.model.MatchingResultModel;
import ru.randomwalk.matcherservice.repository.PersonRepository;
import ru.randomwalk.matcherservice.service.AppointmentSchedulingService;
import ru.randomwalk.matcherservice.service.AvailableTimeService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class AllGroupsInFilterMatchingHandler implements MatchingHandler {

    private final PersonRepository personRepository;
    private final AvailableTimeService availableTimeService;
    private final AppointmentSchedulingService appointmentSchedulingService;

    @Override
    public boolean supports(FilterType filterType) {
        return filterType == FilterType.ALL_MATCH;
    }

    @Override
    @Transactional
    public List<AppointmentDetails> getMatches(Person person) {
        List<UUID> clubsInFilterId = getClubsInFilterIds(person);

        log.info("Searching for partners for person {} with clubs that are all matching to: {}", person.getId(), clubsInFilterId);

        try (Stream<Person> candidateStream =
                     personRepository.findByDistanceAndAllGroupIdsInFilter(
                             person.getId(),
                             person.getLocation().getPosition(),
                             Double.valueOf(person.getSearchAreaInMeters()),
                             clubsInFilterId,
                             clubsInFilterId.size()
                     )
        ) {
            return candidateStream
                    .map(candidate -> scheduleAppointmentIfPossible(person, candidate))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }
    }

    private Optional<AppointmentDetails> scheduleAppointmentIfPossible(
            Person person,
            Person candidate
    ) {
        List<AvailableTimeOverlapModel> timeOverlaps = availableTimeService.getAllAvailableTimeOverlaps(
                person.getAvailableTimes(),
                candidate.getAvailableTimes()
        );

        timeOverlaps.sort(AvailableTimeOverlapModel::compareTo);

        for (var overlap : timeOverlaps) {
            Optional<AppointmentDetails> details = appointmentSchedulingService.scheduleAppointmentWithOverlap(person, candidate, overlap);
            if (details.isPresent()) {
                log.info("Scheduled appointment between {} and {}", person.getId(), candidate.getId());
                return details;
            }
        }
        return Optional.empty();
    }

    private List<UUID> getClubsInFilterIds(Person person) {
        return person.getClubs().stream()
                .filter(Club::isInFilter)
                .map(Club::getClubId)
                .collect(Collectors.toList());
    }
}
