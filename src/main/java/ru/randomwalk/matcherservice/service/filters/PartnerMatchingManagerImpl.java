package ru.randomwalk.matcherservice.service.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.model.AvailableTimeOverlapModel;
import ru.randomwalk.matcherservice.service.AppointmentSchedulingService;
import ru.randomwalk.matcherservice.service.AvailableTimeService;
import ru.randomwalk.matcherservice.service.PersonService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartnerMatchingManagerImpl implements PartnerMatchingManager {

    private final PersonService personService;
    private final AvailableTimeService availableTimeService;
    private final AppointmentSchedulingService appointmentSchedulingService;

    @Override
    @Transactional(readOnly = true)
    public List<Person> findPartnersAndScheduleAppointment(Person person) {
        List<Person> appointedPartners = new ArrayList<>();

        try (Stream<Person> candidateStream = personService.streamSuitableCandidatesForPerson(person)) {
            candidateStream
                    .forEachOrdered(candidate -> scheduleAppointmentIfPossible(person, candidate, appointedPartners));
        }

        log.info("Found {} partners for user {}", appointedPartners.size(), person.getId());
        return appointedPartners;
    }

    private void scheduleAppointmentIfPossible(Person person, Person candidate, List<Person> appointedPartners) {
        try {
            List<AvailableTimeOverlapModel> timeOverlaps = availableTimeService.getAllAvailableTimeOverlaps(
                    person.getAvailableTimes(),
                    candidate.getAvailableTimes()
            );

            for (var overlap : timeOverlaps) {
                Optional<AppointmentDetails> details = appointmentSchedulingService.scheduleAppointmentWithOverlap(person, candidate, overlap);
                if (details.isPresent()) {
                    log.info("Appointment {} was scheduled for users: {} and {}", details.get().getId(), person.getId(), candidate.getId());
                    appointedPartners.add(candidate);
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Error scheduling appointment for {} and {}", person.getId(), candidate.getId(), e);
        }
    }
}
