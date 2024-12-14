package ru.randomwalk.matcherservice.service;

import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.model.enam.FilterType;
import ru.randomwalk.matcherservice.model.entity.Person;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface PersonService {

    Person findByIdWithFetchedAvailableTime(UUID personId);

    /**
     * Returns list of suitable candidates' ids according to person's {@link FilterType}.
     *
     * @param person person to find suitable candidates
     * @return stream of suitable candidates that match person's filters
     */
    List<UUID> getSuitableCandidatesIdsForPerson(Person person);

    Person findById(UUID personId);
    List<Person> findAllByIds(List<UUID> ids);

    void saveAndFlush(Person person);
    void saveAll(List<Person> people);
    void save(Person person);
}
