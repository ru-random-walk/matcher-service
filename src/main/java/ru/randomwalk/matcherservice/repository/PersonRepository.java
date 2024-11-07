package ru.randomwalk.matcherservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.randomwalk.matcherservice.model.entity.Person;

import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {
}
