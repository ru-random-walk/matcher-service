package ru.randomwalk.matcherservice.service.filters;

import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.Person;

import java.util.List;


public interface PartnerMatchingManager {

    /**
     * Finds partners for person and schedules appointments {@link AppointmentDetails} between them.
     * Creates only one appointment for each partner.
     * Prioritize partner who has been in search the longest.
     *
     * @param person person to find partners
     * @return list of partners with whom appointments were scheduled
     */
    List<Person> findPartnersAndScheduleAppointment(Person person);
}
