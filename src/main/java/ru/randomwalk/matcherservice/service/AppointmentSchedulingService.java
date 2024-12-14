package ru.randomwalk.matcherservice.service;

import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.Person;

import java.util.Optional;
import java.util.UUID;

public interface AppointmentSchedulingService {
    Optional<AppointmentDetails> tryToScheduleAppointmentBetweenPeople(UUID person, UUID partner);

}
