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

    @Query("""
        select p from Person as p
        left join fetch p.clubs as cl
        where distance(p.currentPosition, :point) <= :distanceInMeters
        and p.inSearch = true
        and p.id != :excludePersonId
        and (
                (p.groupFilterType = 'ANY_MATCH' and cl.inFilter = true and cl.clubId in :groupIdsInFilter)
                or (p.groupFilterType = 'NO_FILTER' and (:isAnyMatchSearch = false or cl.clubId in :groupIdsInFilter))
                or (:isAnyMatchSearch = false or (p.groupFilterType = 'ALL_MATCH' and cl.clubId in :groupIdsInFilter))
            )
        order by p.inSearchFrom
    """
    )
    @QueryHints(
            @QueryHint(name = AvailableHints.HINT_FETCH_SIZE, value = "20")
    )
    Stream<Person> streamPersonByDistanceAndGroupIdsInFilterByFilterType(
            @Param("excludePersonId") UUID excludePersonId,
            @Param("point") Point point,
            @Param("distanceInMeters") Double distanceInMeters,
            @Param("groupIdsInFilter") @Nullable List<UUID> groupIds,
            @Param("isAnyMatchSearch") boolean isAnyMatchSearch
    );

    @Query(value = """
        SELECT p.id FROM PERSON p
            LEFT JOIN person_club pc ON p.id = pc.person_id
        WHERE p.id != :excludePersonId
            AND ST_DWithin(p.current_postion, :point, :distanceInMeters) = true
            AND p.in_search = true
            AND (p.group_filter_type = 'NO_FILTER' OR pc.in_filter = true)
            AND pc.club_id IN :groupIdsInFilter
        GROUP BY p.id
        HAVING COUNT(DISTINCT pc.club_id) = :filterGroupCount
        ORDER BY p.in_search_from
    """, nativeQuery = true
    )
    List<UUID> findByDistanceAndAllGroupIdsInFilter(
            @Param("excludePersonId") UUID excludePersonId,
            @Param("point") Point point,
            @Param("distanceInMeters") Double distanceInMeters,
            @Param("groupIdsInFilter") List<UUID> groupIds,
            @Param("filterGroupCount") Integer filterGroupCount
    );
}
