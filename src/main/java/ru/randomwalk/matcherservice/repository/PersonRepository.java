package ru.randomwalk.matcherservice.repository;

import jakarta.annotation.Nullable;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.AvailableHints;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;
import ru.randomwalk.matcherservice.model.entity.Person;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface PersonRepository extends JpaRepository<Person, UUID> {

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"availableTimes", "dayLimit"})
    @Query("""
        select p from Person as p
        inner join p.location as l
        inner join p.clubs as cl
        where within(l.position, :point, :distanceInMeters) = true
        and p.inSearch = true
        and p.id != :excludePersonId
        and ((p.groupFilterType = 'ANY_MATCH' and cl.inFilter = true and cl.clubId in :groupIdsInFilter)
        or (p.groupFilterType = 'NO_FILTER' and cl.clubId in :groupIdsInFilter))
    """
    )
    @QueryHints(
            @QueryHint(name = AvailableHints.HINT_FETCH_SIZE, value = "20")
    )
    //TODO: Не забыть, что можем подобрать с фильтром ALL_MATCH, нужно дополнительно проверять на соответсвие в коде
    Stream<Person> findByDistanceAndAnyGroupIdsInFilter(
            @Param("excludePersonId") UUID excludePersonId,
            @Param("appointmentStatus") AppointmentStatus appointmentStatus,
            @Param("point") Point point,
            @Param("distanceInMeters") Double distanceInMeters,
            @Param("groupIdsInFilter") @Nullable List<UUID> groupIds
    );

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"availableTimes", "dayLimit"})
    @Query(value = """
        SELECT p.* FROM PERSON p
            JOIN person_club pc ON p.id = pc.person_id
            JOIN location l ON p.location_id = l.id
        WHERE p.id != :excludePersonId
        AND p.in_search = true
        AND pc.in_filter = true
        AND ST_DWithin(l.position, :point, :distanceInMeters) = true
        AND pc.club_id IN :groupIdsInFilter
        GROUP BY p.id
        HAVING COUNT(DISTINCT pc.club_id) = :filterGroupCount
        ORDER BY p.in_search_from
    """, nativeQuery = true
    )
    @QueryHints(
            @QueryHint(name = AvailableHints.HINT_FETCH_SIZE, value = "20")
    )
    Stream<Person> findByDistanceAndAllGroupIdsInFilter(
            @Param("excludePersonId") UUID excludePersonId,
            @Param("point") Point point,
            @Param("distanceInMeters") Double distanceInMeters,
            @Param("groupIdsInFilter") List<UUID> groupIds,
            @Param("filterGroupCount") Integer filterGroupCount
    );
}
