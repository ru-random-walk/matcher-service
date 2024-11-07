package ru.randomwalk.matcherservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;

import java.util.UUID;

public interface AppointmentDetailsRepository extends JpaRepository<AppointmentDetails, UUID> {
}
