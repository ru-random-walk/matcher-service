package ru.randomwalk.matcherservice.service;

import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.randomwalk.matcherservice.model.entity.Club;
import ru.randomwalk.matcherservice.model.entity.Person;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PersonService {

    Person findByIdWithFetchedAvailableTime(UUID personId);

    List<Person> findAllWithFetchedClubs(Collection<UUID> ids);

    Person findById(UUID personId);

    List<Person> findAllByIds(List<UUID> ids);

    void saveAndFlush(Person person);

    void saveAll(List<Person> people);

    void save(Person person);

    List<Club> getClubsForPerson(UUID personId);

    void addNewPerson(RegisteredUserInfoEvent addPersonDto);

    int getClubsSimilarityBetweenPeople(Person first, Person second);

}
