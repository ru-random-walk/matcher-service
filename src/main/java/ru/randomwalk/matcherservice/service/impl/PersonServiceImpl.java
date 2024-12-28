package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.model.dto.request.AddPersonDto;
import ru.randomwalk.matcherservice.model.enam.FilterType;
import ru.randomwalk.matcherservice.model.entity.Club;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.exception.MatcherBadRequestException;
import ru.randomwalk.matcherservice.model.exception.MatcherNotFoundException;
import ru.randomwalk.matcherservice.repository.ClubRepository;
import ru.randomwalk.matcherservice.repository.PersonRepository;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.mapper.PersonMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.randomwalk.matcherservice.model.enam.FilterType.ALL_MATCH;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final ClubRepository clubRepository;
    private final PersonMapper personMapper;

    @Override
    public Person findByIdWithFetchedAvailableTime(UUID personId) {
        return personRepository.findByIdWithFetchedAvailableTime(personId)
                .orElseThrow(() -> new MatcherNotFoundException("Person with id %s does not exist", personId));
    }

    @Override
    public Person findById(UUID personId) {
        return personRepository.findById(personId)
                .orElseThrow(() -> new MatcherNotFoundException("Person with id %s does not exist", personId));
    }

    @Override
    public List<Person> findAllByIds(List<UUID> ids) {
        return personRepository.findAllById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getSuitableCandidatesIdsForPerson(Person person) {
        log.info("Searching for suitable candidates for {}", person.getId());
        return switch (person.getGroupFilterType()) {
            case ALL_MATCH -> getAllMatchPersonStreamForPerson(person);
            case ANY_MATCH -> getCandidatesForAnyMatchFilterPerson(person);
            case NO_FILTER -> getCandidatesForWithoutFilterPerson(person);
        };
    }

    private List<UUID> getAllMatchPersonStreamForPerson(Person person) {
        List<UUID> clubsInFilterId = getClubsInFilterIds(person);

        log.info("Searching for partners for person {} with clubs that are all matching to: {}", person.getId(), clubsInFilterId);

        return personRepository.findByDistanceAndAllGroupIdsInFilter(
                person.getId(),
                person.getCurrentPosition(),
                Double.valueOf(person.getSearchAreaInMeters()),
                clubsInFilterId,
                clubsInFilterId.size()
        );
    }

    private List<UUID> getCandidatesForAnyMatchFilterPerson(Person person) {
        List<UUID> clubsInFilterId = getClubsInFilterIds(person);
        Set<UUID> clubsIdsSet = new HashSet<>(clubsInFilterId);

        log.info("Searching for partners for person {} with any club from list: {}", person.getId(), clubsInFilterId);

        return personRepository.streamPersonByDistanceAndGroupIdsInFilterByFilterType(
                person.getId(),
                person.getCurrentPosition(),
                Double.valueOf(person.getSearchAreaInMeters()),
                clubsInFilterId,
                true
        )
                .filter(candidate -> filterAllGroupsMatchIfNeeded(candidate, clubsIdsSet))
                .map(Person::getId)
                .collect(Collectors.toList());
    }

    private List<UUID> getCandidatesForWithoutFilterPerson(Person person) {
        log.info("Searching for partners for person {} with no filter", person.getId());
        List<UUID> clubIds = person.getClubs().stream()
                .map(Club::getClubId)
                .collect(Collectors.toList());

        return personRepository.streamPersonByDistanceAndGroupIdsInFilterByFilterType(
                person.getId(),
                person.getCurrentPosition(),
                Double.valueOf(person.getSearchAreaInMeters()),
                clubIds,
                false
        )
                .map(Person::getId)
                .collect(Collectors.toList());
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

    @Override
    @Transactional
    public List<Club> changeClubsInFilter(UUID personId, FilterType filterType, List<UUID> clubsInFilterIds) {
        Person person = findById(personId);
        person.setGroupFilterType(filterType);
        Set<UUID> newClubsInFilter = new HashSet<>(clubsInFilterIds);

        var clubs = person.getClubs();
        clubs.forEach(club -> club.setInFilter(false));
        clubs.stream()
                .filter(club -> newClubsInFilter.contains(club.getClubId()))
                .forEach(club -> club.setInFilter(true));

        person.setClubs(clubs);
        clubRepository.saveAll(clubs);
        personRepository.save(person);

        return clubs;
    }

    @Override
    public List<Club> getClubsForPerson(UUID personId, Boolean inFilter) {
        return clubRepository.findByPersonIdAndInFilterIs(personId, inFilter);
    }

    @Override
    @Transactional
    public void changeCurrentLocation(UUID personId, Double longitude, Double latitude, Integer searchAreaInMeters) {
        Person person = findById(personId);

        person.setLatitude(latitude);
        person.setLongitude(longitude);
        person.setSearchAreaInMeters(searchAreaInMeters);

        save(person);
        log.info("Person {} location has been changed", personId);
    }

    @Override
    public void addNewPerson(AddPersonDto addPersonDto) {
        if (personRepository.existsById(addPersonDto.id())) {
            throw new MatcherBadRequestException("Person with id %s already exists", addPersonDto.id());
        }
        Person person = personMapper.createPersonEntity(addPersonDto);
        save(person);
    }

    private List<UUID> getClubsInFilterIds(Person person) {
        return person.getClubs().stream()
                .filter(Club::isInFilter)
                .map(Club::getClubId)
                .collect(Collectors.toList());
    }

}
