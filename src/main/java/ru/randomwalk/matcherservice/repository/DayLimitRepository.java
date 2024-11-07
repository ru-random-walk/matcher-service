package ru.randomwalk.matcherservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.randomwalk.matcherservice.model.entity.DayLimit;

public interface DayLimitRepository extends JpaRepository<DayLimit, DayLimit.DayLimitId> {
}
