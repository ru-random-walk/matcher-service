package ru.randomwalk.matcherservice.service;

import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.model.AvailableTimeOverlapModel;

import java.util.Optional;

public interface AppointmentSchedulingService {
    Optional<AppointmentDetails> tryToScheduleAppointmentBetweenPeople(Person person, Person partner);

}
