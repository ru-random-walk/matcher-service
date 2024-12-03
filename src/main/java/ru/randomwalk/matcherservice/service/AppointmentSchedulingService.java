package ru.randomwalk.matcherservice.service;

import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.model.AvailableTimeOverlapModel;

public interface AppointmentSchedulingService {
    void scheduleAppointmentWithOverlap(Person person, Person partner, AvailableTimeOverlapModel overlapModel);
}
