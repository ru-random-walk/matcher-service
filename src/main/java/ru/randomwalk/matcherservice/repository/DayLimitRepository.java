package ru.randomwalk.matcherservice.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import ru.randomwalk.matcherservice.model.entity.DayLimit;

import java.util.Optional;

public interface DayLimitRepository extends JpaRepository<DayLimit, DayLimit.DayLimitId> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value ="5000")})
    @Query("select dl from DayLimit as dl where dl.dayLimitId = :dayLimitId")
    Optional<DayLimit> findByIdWithLock(DayLimit.DayLimitId dayLimitId);
}
