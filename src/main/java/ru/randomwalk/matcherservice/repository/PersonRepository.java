package ru.randomwalk.matcherservice.repository;

import jakarta.annotation.Nullable;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.AvailableHints;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import ru.randomwalk.matcherservice.model.entity.Person;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface PersonRepository extends JpaRepository<Person, UUID> {

    @Query("""
            select p from Person as p
            left join fetch p.availableTimes as a
            left join fetch a.dayLimit as dl
            where p.id = :id
    """)
    Optional<Person> findByIdWithFetchedAvailableTime(@Param("id") UUID id);

    @Query("select p from Person as p left join fetch p.clubs where p.id in :ids")
    List<Person> findAllWithFetchedClubs(@Param("ids") Collection<UUID> ids);
}
