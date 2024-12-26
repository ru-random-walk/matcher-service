package ru.randomwalk.matcherservice.service;

import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.entity.projection.AppointmentPartner;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentDetailsService {
    AppointmentDetails createAppointment(Person person, Person partner, OffsetDateTime startsAt);

    List<AppointmentPartner> getAllPartnerIdsForPersonAppointments(UUID personId, List<AppointmentDetails> appointments);
}
