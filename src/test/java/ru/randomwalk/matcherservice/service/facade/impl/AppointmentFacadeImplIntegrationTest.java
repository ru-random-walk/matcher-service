package ru.randomwalk.matcherservice.service.facade.impl;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.AbstractPostgresContainerTest;
import ru.randomwalk.matcherservice.model.entity.DayLimit;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.repository.AppointmentDetailsRepository;
import ru.randomwalk.matcherservice.repository.DayLimitRepository;
import ru.randomwalk.matcherservice.repository.PersonRepository;
import ru.randomwalk.matcherservice.service.AppointmentDetailsService;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class AppointmentFacadeImplIntegrationTest extends AbstractPostgresContainerTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private DayLimitRepository dayLimitRepository;

    @Autowired
    private AppointmentDetailsRepository appointmentDetailsRepository;

    @Autowired
    private AppointmentDetailsService service;

    @Autowired
    private AppointmentFacadeImpl appointmentFacade;

    @Test
    @Transactional
    @Rollback
    void cancelAppointment() {
        OffsetDateTime startsAt = OffsetDateTime.now(ZoneId.of("UTC")).minusDays(1);
        Person person = createPersonWithDayLimit(0, startsAt.toLocalDate());
        Person partner = createPersonWithDayLimit(0, startsAt.toLocalDate());
        Point location = GeometryUtil.createPoint(41.881832, -87.623177);
        var appointment = service.createAppointment(person.getId(), partner.getId(), startsAt, location);

        appointmentFacade.cancelAppointment(appointment.getId(), person.getId().toString());

        assertEquals(0, getWalkCount(person.getId(), startsAt.toLocalDate()));
        assertEquals(1, getWalkCount(partner.getId(), startsAt.toLocalDate()));
        assertFalse(appointmentDetailsRepository.findById(appointment.getId()).isPresent());
        assertEquals(0, appointmentDetailsRepository.getAppointmentPartnerIds(appointment.getId()).size());
    }

    private Person createPersonWithDayLimit(Integer walkCount, LocalDate date) {
        var person = new Person();
        person.setId(UUID.randomUUID());
        var personToReturn =  personRepository.save(person);

        var dayLimit = new DayLimit();
        dayLimit.setDayLimitId(new DayLimit.DayLimitId(personToReturn.getId(), date));
        dayLimit.setWalkCount(walkCount);
        dayLimitRepository.save(dayLimit);

        return person;
    }

    private Integer getWalkCount(UUID personId, LocalDate date) {
        return dayLimitRepository.findById(new DayLimit.DayLimitId(personId, date))
                .map(DayLimit::getWalkCount)
                .orElse(-1);
    }
}