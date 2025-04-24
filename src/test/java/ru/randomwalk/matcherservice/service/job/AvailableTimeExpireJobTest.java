package ru.randomwalk.matcherservice.service.job;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.randomwalk.matcherservice.AbstractContainerTest;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(MockitoExtension.class)
class AvailableTimeExpireJobTest extends AbstractContainerTest {

    @Autowired
    private AvailableTimeService availableTimeService;
    @Autowired
    private PersonService personService;

    @Autowired
    private DayLimitRepository dayLimitRepository;

    @Autowired
    private AvailableTimeExpireJob availableTimeExpireJob;

    @Autowired
    private AvailableTimeRepository availableTimeRepository;

    private static final int JOBS_TO_DELETE_COUNT = 10;
    private static final int NOT_DELETED_JOBS_COUNT = 3;

    @Transactional
    @Rollback
    @Test
    public void allPreviousTimesAreDeleted() throws JobExecutionException {
        for (int i = 1; i <= JOBS_TO_DELETE_COUNT; ++i) {
            UUID personId = UUID.randomUUID();
            personService.addNewPerson(new RegisteredUserInfoEvent(personId, personId.toString()));
            addAvailableTimeForPerson(
                    personId,
                    GeometryUtil.createPoint(1,2),
                    LocalTime.now(),
                    LocalTime.now().plusHours(2),
                    1,
                    LocalDate.now().minusDays(i)
            );
        }

        for (int i = 0; i < NOT_DELETED_JOBS_COUNT; ++i) {
            UUID personId = UUID.randomUUID();
            personService.addNewPerson(new RegisteredUserInfoEvent(personId, personId.toString()));
            addAvailableTimeForPerson(
                    personId,
                    GeometryUtil.createPoint(1,2),
                    LocalTime.now().minusHours(1),
                    LocalTime.now().plusHours(2),
                    1,
                    LocalDate.now().plusDays(i)
            );
        }

        availableTimeExpireJob.execute(null);

        List<AvailableTime> totalTimes = availableTimeRepository.findAll();
        List<DayLimit> totalDayLimits = dayLimitRepository.findAll();
        assertEquals(NOT_DELETED_JOBS_COUNT, totalTimes.size());
        assertEquals(NOT_DELETED_JOBS_COUNT, totalDayLimits.size());
    }

    private AvailableTime addAvailableTimeForPerson(UUID personId, Point point, LocalTime from, LocalTime to, Integer dayLimit, LocalDate date) {
        AvailableTime availableTime = new AvailableTime();

        availableTime.setDate(date);
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