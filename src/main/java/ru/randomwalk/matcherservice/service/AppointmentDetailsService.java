package ru.randomwalk.matcherservice.service;

import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.Person;

import java.time.OffsetDateTime;

public interface AppointmentDetailsService {
    AppointmentDetails createAppointment(Person person, Person partner, OffsetDateTime startsAt);
}
