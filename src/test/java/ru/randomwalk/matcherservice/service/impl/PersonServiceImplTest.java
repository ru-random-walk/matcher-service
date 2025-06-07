package ru.randomwalk.matcherservice.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.randomwalk.matcherservice.AbstractContainerTest;
import ru.randomwalk.matcherservice.repository.PersonRepository;
import ru.randomwalk.matcherservice.service.PersonService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class PersonServiceImplTest extends AbstractContainerTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Test
    void addNewOrUpdateExistingPerson() {
        RegisteredUserInfoEvent event = new RegisteredUserInfoEvent(UUID.randomUUID(), "Name1");

        personService.addNewOrUpdateExistingPerson(event);

        assertTrue(personRepository.existsById(event.id()));

        RegisteredUserInfoEvent update = new RegisteredUserInfoEvent(event.id(), "Name2");
        personService.addNewOrUpdateExistingPerson(update);

        assertEquals(update.fullName(), personRepository.findById(event.id()).get().getFullName());
    }
}