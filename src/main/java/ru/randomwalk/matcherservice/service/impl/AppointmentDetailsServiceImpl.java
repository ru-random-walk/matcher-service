package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.entity.projection.AppointmentPartner;
import ru.randomwalk.matcherservice.model.exception.MatcherNotFoundException;
import ru.randomwalk.matcherservice.repository.AppointmentDetailsRepository;
import ru.randomwalk.matcherservice.service.AppointmentDetailsService;
import ru.randomwalk.matcherservice.service.PersonService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentDetailsServiceImpl implements AppointmentDetailsService {

    private final AppointmentDetailsRepository appointmentDetailsRepository;
    private final PersonService personService;

    @Override
    public AppointmentDetails createAppointment(UUID personId, UUID partnerId, OffsetDateTime startsAt, Point approximateLocation) {
        log.info("Create appointment for people with ids: {}, {}. Starts at: {}", personId, partnerId, startsAt);
        AppointmentDetails appointmentDetails = new AppointmentDetails();
        appointmentDetails.setStatus(AppointmentStatus.APPOINTED);
        appointmentDetails.setStartsAt(startsAt);
        appointmentDetails.setLocation(approximateLocation);

        appointmentDetails = appointmentDetailsRepository.save(appointmentDetails);
        linkMembersToAppointment(appointmentDetails, personId, partnerId);

        log.info("Appointment {} created", appointmentDetails.getId());
        return appointmentDetails;
    }

    @Override
    public List<AppointmentPartner> getAllPartnerIdsForPersonAppointments(UUID personId, List<AppointmentDetails> appointments) {
        List<UUID> appointmentIds = appointments.stream()
                .map(AppointmentDetails::getId)
                .toList();

        return appointmentDetailsRepository.getAllPartnerIdsForAppointmentsOfPerson(personId, appointmentIds);
    }

    @Override
    public List<UUID> getAppointmentParticipants(UUID appointmentId) {
        return appointmentDetailsRepository.getAppointmentPartnerIds(appointmentId);
    }

    @Override
    public AppointmentDetails getById(UUID appointmentId) {
        return appointmentDetailsRepository.findById(appointmentId)
                .orElseThrow(() -> new MatcherNotFoundException("Appointment with id %s does not exist", appointmentId));
    }

    @Override
    @Transactional
    public void deleteById(UUID appointmentId) {
        appointmentDetailsRepository.deleteById(appointmentId);
    }

    private void linkMembersToAppointment(AppointmentDetails appointmentDetails, UUID... membersIds) {
        List<Person> people = personService.findAllByIds(List.of(membersIds));
        people.forEach(person -> person.getAppointments().add(appointmentDetails));
        personService.saveAll(people);
    }
}
