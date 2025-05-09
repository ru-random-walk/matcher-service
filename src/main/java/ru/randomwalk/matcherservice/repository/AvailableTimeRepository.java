package ru.randomwalk.matcherservice.repository;


import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.List;
import java.util.UUID;

public interface AvailableTimeRepository extends JpaRepository<AvailableTime, UUID> {

    @Query(value = """
        select at from AvailableTime as at
        join fetch at.dayLimit as dl
        where distance(at.location.point, :location) <= :searchDistance
        and dl.walkCount > 0
        and at.personId != :personId
        and at.date = :matchingDate
        and (at.timeFrom <= :timeUntil AND at.timeUntil >= :timeFrom)
    """)
    List<AvailableTime> findMatchingAvailableTimes(
            @Param("personId") UUID personIdToExclude,
            @Param("location") Point location,
            @Param("searchDistance") Integer searchDistance,
            @Param("matchingDate") LocalDate matchingDate,
            @Param("timeFrom") OffsetTime timeFrom,
            @Param("timeUntil") OffsetTime timeUntil
    );

    @Modifying
    @Query("delete from AvailableTime at where at.date < :date")
    int deleteAllByDateBefore(LocalDate date);
}
