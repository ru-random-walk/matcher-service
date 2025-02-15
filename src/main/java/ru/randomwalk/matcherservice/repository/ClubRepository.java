package ru.randomwalk.matcherservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.randomwalk.matcherservice.model.entity.Club;

import java.util.List;
import java.util.UUID;

public interface ClubRepository extends JpaRepository<Club, Club.PersonClubId> {

    List<Club> findByPersonId(UUID personId);
}
