package ru.randomwalk.matcherservice.service;

import ru.randomwalk.matcherservice.model.enam.FilterType;
import ru.randomwalk.matcherservice.model.entity.Person;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface PersonService {

    Person findByIdWithFetchedAvailableTime(UUID personId);

    /**
     * Returns stream of suitable candidates according to person's {@link FilterType}.
     * <p><b>ATTENTION!</b> This method should be invoked in one jdbc session!</p>
     *
     * @param person person to find suitable candidates
     * @return stream of suitable candidates that match person's filters
     */
    Stream<Person> streamSuitableCandidatesForPerson(Person person);

    void saveAndFlush(Person person);
    void saveAll(List<Person> people);
    void save(Person person);
}
