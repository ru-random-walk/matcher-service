package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.repository.AppointmentDetailsRepository;
import ru.randomwalk.matcherservice.service.AppointmentDetailsService;
import ru.randomwalk.matcherservice.service.PersonService;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentDetailsServiceImpl implements AppointmentDetailsService {

    private final AppointmentDetailsRepository appointmentDetailsRepository;
    private final PersonService personService;

    @Override
    public AppointmentDetails createAppointment(Person person, Person partner, OffsetDateTime startsAt) {
        log.info("Create appointment for people with ids: {}, {}. Starts at: {}", person.getId(), partner.getId(), startsAt);
        AppointmentDetails appointmentDetails = new AppointmentDetails();
        appointmentDetails.setStatus(AppointmentStatus.APPOINTED);
        appointmentDetails.setStartsAt(startsAt);

        appointmentDetails = appointmentDetailsRepository.save(appointmentDetails);

        addPerson(appointmentDetails, person);
        addPerson(appointmentDetails, partner);

        log.info("Appointment {} created", appointmentDetails.getId());
        return appointmentDetails;
    }

    private void addPerson(AppointmentDetails appointmentDetails, Person person) {
        person.getAppointments().add(appointmentDetails);
        personService.save(person);
    }
}
