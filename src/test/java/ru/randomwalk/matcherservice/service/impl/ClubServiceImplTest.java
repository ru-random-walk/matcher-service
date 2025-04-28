package ru.randomwalk.matcherservice.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.random.walk.dto.UserExcludeEvent;
import ru.random.walk.dto.UserJoinEvent;
import ru.randomwalk.matcherservice.AbstractContainerTest;
import ru.randomwalk.matcherservice.model.entity.Club;
import ru.randomwalk.matcherservice.repository.ClubRepository;
import ru.randomwalk.matcherservice.service.ClubService;
import ru.randomwalk.matcherservice.service.PersonService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class ClubServiceImplTest extends AbstractContainerTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private ClubService clubService;

    @Autowired
    private ClubRepository clubRepository;

    @Transactional
    @Rollback
    @Test
    void addUserToClub() {
        UUID personId = UUID.randomUUID();
        UUID clubId = UUID.randomUUID();
        personService.addNewPerson(new RegisteredUserInfoEvent(personId, "name"));

        clubService.addUserToClub(new UserJoinEvent(personId, clubId));

        assertTrue(clubRepository.findById(new Club.PersonClubId(personId, clubId)).isPresent());
    }

    @Transactional
    @Rollback
    @Test
    void removeUserFromClub() {
        UUID personId = UUID.randomUUID();
        UUID clubId = UUID.randomUUID();
        personService.addNewPerson(new RegisteredUserInfoEvent(personId, "name"));
        clubService.addUserToClub(new UserJoinEvent(personId, clubId));

        clubService.removeUserFromClub(new UserExcludeEvent(personId, clubId));

        assertTrue(clubRepository.findById(new Club.PersonClubId(personId, clubId)).isEmpty());
    }

    @Transactional
    @Rollback
    @Test
    void removeUserFromClub_doesNotThrowsIfAlreadyRemoved() {
        UUID personId = UUID.randomUUID();
        UUID clubId = UUID.randomUUID();
        personService.addNewPerson(new RegisteredUserInfoEvent(personId, "name"));

        assertTrue(clubRepository.findById(new Club.PersonClubId(personId, clubId)).isEmpty());
        assertDoesNotThrow(() -> clubService.removeUserFromClub(new UserExcludeEvent(personId, clubId)));
    }
}