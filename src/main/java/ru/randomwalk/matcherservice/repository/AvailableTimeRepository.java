package ru.randomwalk.matcherservice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;

import java.util.UUID;

public interface AvailableTimeRepository extends JpaRepository<AvailableTime, UUID> {

}
