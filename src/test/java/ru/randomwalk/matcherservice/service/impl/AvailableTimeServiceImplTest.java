package ru.randomwalk.matcherservice.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.randomwalk.matcherservice.AbstractContainerTest;
import ru.randomwalk.matcherservice.model.dto.AvailableTimeModifyDto;
import ru.randomwalk.matcherservice.model.dto.LocationDto;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.DayLimit;
import ru.randomwalk.matcherservice.model.entity.Location;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.repository.AvailableTimeRepository;
import ru.randomwalk.matcherservice.repository.DayLimitRepository;
import ru.randomwalk.matcherservice.service.AvailableTimeService;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class AvailableTimeServiceImplTest extends AbstractContainerTest {

    @Autowired
    private AvailableTimeService availableTimeService;

    @Autowired
    private AvailableTimeRepository repository;

    @Autowired
    private DayLimitRepository dayLimitRepository;

    @Autowired
    private PersonService personService;

    private final static Point location = GeometryUtil.createPoint(-30.262667, 42.032974);

    @Transactional
    @Rollback
    @Test
    void replaceExistingAvailableTime() {
        var personId = UUID.randomUUID();
        personService.addNewPerson(new RegisteredUserInfoEvent(personId, "initial"));
        var time = addAvailableTimeForPerson(personId, location, LocalTime.now(), LocalTime.now().plusHours(1), 2);
        AvailableTimeModifyDto modifyDto = new AvailableTimeModifyDto(
                LocalDate.now().plusDays(1),
                OffsetTime.now().minusHours(2),
                OffsetTime.now().plusHours(4),
                new LocationDto(0.33, 0.55, "Birmingham", "street", "building"),
                List.of(UUID.randomUUID())
        );

        availableTimeService.replaceExistingAvailableTime(time.getId(), modifyDto);

        assertNotEquals(modifyDto.date(), time.getDate());
        assertEquals(modifyDto.timeUntil(), time.getTimeUntil());
        assertEquals(modifyDto.timeFrom(), time.getTimeFrom());
        assertEquals(modifyDto.clubsInFilter(), new ArrayList<>(time.getClubsInFilter()));
        assertEquals(modifyDto.location().latitude(), time.getLocation().getLatitude());
        assertEquals(modifyDto.location().longitude(), time.getLocation().getLongitude());
        assertEquals(modifyDto.location().city(), time.getLocation().getCity());
        assertEquals(modifyDto.location().building(), time.getLocation().getBuilding());
        assertEquals(modifyDto.location().street(), time.getLocation().getStreet());
    }

    @Transactional
    @Rollback
    @Test
    void deleteAvailableTime() {
        var personId = UUID.randomUUID();
        personService.addNewPerson(new RegisteredUserInfoEvent(personId, "initial"));
        var time = addAvailableTimeForPerson(personId, location, LocalTime.now(), LocalTime.now().plusHours(1), 2);

        availableTimeService.deleteAvailableTime(time);

        var person = personService.findById(personId);
        assertEquals(0, person.getAvailableTimes().size());
        assertTrue(repository.findById(time.getId()).isEmpty());
    }

    private AvailableTime addAvailableTimeForPerson(UUID personId, Point point, LocalTime from, LocalTime to, Integer dayLimit) {
        AvailableTime availableTime = new AvailableTime();

        availableTime.setDate(LocalDate.now());
        availableTime.setTimeFrom(OffsetTime.of(from, ZoneOffset.UTC));
        availableTime.setTimeUntil(OffsetTime.of(to, ZoneOffset.UTC));
        availableTime.setTimezone(ZoneOffset.UTC.getId());
        availableTime.setPersonId(personId);

        if (dayLimit != null) {
            DayLimit limit = new DayLimit();
            limit.setDayLimitId(new DayLimit.DayLimitId(personId, availableTime.getDate()));
            limit.setWalkCount(dayLimit);
            dayLimitRepository.save(limit);
            availableTime.setDayLimit(limit);
        }

        Location location = new Location();
        location.setPoint(point);
        availableTime.setLocation(location);
        availableTimeService.addAvailableTime(availableTime, personId);

        return availableTime;
    }
}