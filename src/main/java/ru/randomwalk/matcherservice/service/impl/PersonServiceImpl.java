package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.randomwalk.matcherservice.model.entity.Club;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.exception.MatcherBadRequestException;
import ru.randomwalk.matcherservice.model.exception.MatcherNotFoundException;
import ru.randomwalk.matcherservice.repository.ClubRepository;
import ru.randomwalk.matcherservice.repository.PersonRepository;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.mapper.PersonMapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public List<Person> findAllWithFetchedClubs(Collection<UUID> ids) {
        return personRepository.findAllWithFetchedClubs(ids);
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
    public List<Club> getClubsForPerson(UUID personId) {
        return clubRepository.findByPersonId(personId);
    }

    @Override
    public void addNewOrUpdateExistingPerson(RegisteredUserInfoEvent userInfoEvent) {
        log.info("Refreshing information about person: {}", userInfoEvent);
        var person = personMapper.createPersonEntity(userInfoEvent);
        save(person);
    }

    @Override
    public int getClubsSimilarityBetweenPeople(Person first, Person second) {
        Set<UUID> secondPersonClubIds = second.getClubs().stream().map(Club::getClubId).collect(Collectors.toSet());
        return (int) first.getClubs().stream()
                .map(Club::getClubId)
                .filter(secondPersonClubIds::contains)
                .count();
    }

    @Override
    public List<Person> findAllWithFetchedAppointments(List<UUID> ids) {
        return personRepository.findAllByIdsWithFetchedAppointments(ids);
    }

}
