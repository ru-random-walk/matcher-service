package ru.randomwalk.matcherservice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;

import java.util.List;
import java.util.UUID;

public interface AvailableTimeRepository extends JpaRepository<AvailableTime, UUID> {

    List<AvailableTime> findByPersonId(UUID personId);

}
