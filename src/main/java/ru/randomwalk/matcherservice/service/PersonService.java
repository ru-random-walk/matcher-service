package ru.randomwalk.matcherservice.service;

import ru.randomwalk.matcherservice.model.entity.Person;

import java.util.UUID;

public interface PersonService {
    Person findById(UUID personId);

    void save(Person person);
}
