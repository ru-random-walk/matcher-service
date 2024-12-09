package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.model.entity.Club;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.exception.MatcherNotFoundException;
import ru.randomwalk.matcherservice.repository.PersonRepository;
import ru.randomwalk.matcherservice.service.PersonService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.randomwalk.matcherservice.model.enam.FilterType.ALL_MATCH;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    @Override
    public Person findByIdWithFetchedAvailableTime(UUID personId) {
        return personRepository.findByIdWithFetchedAvailableTime(personId)
                .orElseThrow(() -> new MatcherNotFoundException("Person with id %d does not exist", personId));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Stream<Person> streamSuitableCandidatesForPerson(Person person) {
        log.info("Searching for suitable candidates for {}", person.getId());
        return switch (person.getGroupFilterType()) {
            case ALL_MATCH -> getAllMatchPersonStreamForPerson(person);
            case ANY_MATCH -> getAnyMatchPersonStreamForPerson(person);
            case NO_FILTER -> getWithoutFilterPersonStreamForPerson(person);
        };
    }

    private Stream<Person> getAllMatchPersonStreamForPerson(Person person) {
        List<UUID> clubsInFilterId = getClubsInFilterIds(person);

        log.info("Searching for partners for person {} with clubs that are all matching to: {}", person.getId(), clubsInFilterId);

        return personRepository.findByDistanceAndAllGroupIdsInFilter(
                person.getId(),
                person.getLocation().getPosition(),
                Double.valueOf(person.getSearchAreaInMeters()),
                clubsInFilterId,
                clubsInFilterId.size()
        );
    }

    private Stream<Person> getAnyMatchPersonStreamForPerson(Person person) {
        List<UUID> clubsInFilterId = getClubsInFilterIds(person);
        Set<UUID> clubsIdsSet = new HashSet<>(clubsInFilterId);

        log.info("Searching for partners for person {} with any club from list: {}", person.getId(), clubsInFilterId);

        return personRepository.findByDistanceAndGroupIdsInFilterByFilterType(
                person.getId(),
                person.getLocation().getPosition(),
                Double.valueOf(person.getSearchAreaInMeters()),
                clubsInFilterId,
                true
        ).filter(candidate -> filterAllGroupsMatchIfNeeded(candidate, clubsIdsSet));
    }

    private Stream<Person> getWithoutFilterPersonStreamForPerson(Person person) {
        List<UUID> clubsInFilterId = getClubsInFilterIds(person);
        Set<UUID> clubsIdsSet = new HashSet<>(clubsInFilterId);

        log.info("Searching for partners for person {} with no filter", person.getId());

        return personRepository.findByDistanceAndGroupIdsInFilterByFilterType(
                person.getId(),
                person.getLocation().getPosition(),
                Double.valueOf(person.getSearchAreaInMeters()),
                clubsInFilterId,
                false
        ).filter(candidate -> filterAllGroupsMatchIfNeeded(candidate, clubsIdsSet));
    }

    private boolean filterAllGroupsMatchIfNeeded(Person candidate, Set<UUID> clubsInFilterId) {
        if (candidate.getGroupFilterType() != ALL_MATCH) {
            return true;
        }

        return candidate.getClubs().stream()
                .map(Club::getClubId)
                .allMatch(clubsInFilterId::contains);
    }

    @Override
    public void saveAndFlush(Person person) {
        personRepository.saveAndFlush(person);
    }

    @Override
    public void saveAll(List<Person> people) {
        personRepository.saveAll(people);
    }

    @Override
    public void save(Person person) {
        personRepository.save(person);
    }

    private List<UUID> getClubsInFilterIds(Person person) {
        return person.getClubs().stream()
                .filter(Club::isInFilter)
                .map(Club::getClubId)
                .collect(Collectors.toList());
    }

}
