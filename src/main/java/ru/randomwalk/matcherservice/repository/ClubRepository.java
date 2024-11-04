package ru.randomwalk.matcherservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.randomwalk.matcherservice.model.entity.Club;

public interface ClubRepository extends JpaRepository<Club, Club.PersonClubId> {
}
