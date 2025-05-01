package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.DayLimit;
import ru.randomwalk.matcherservice.model.exception.MatcherNotFoundException;
import ru.randomwalk.matcherservice.repository.DayLimitRepository;
import ru.randomwalk.matcherservice.service.DayLimitService;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DayLimitServiceImpl implements DayLimitService {

    private final DayLimitRepository dayLimitRepository;

    @Override
    @Transactional
    public void incrementDayLimitForPersonAndDate(UUID personId, LocalDate date) {
        var dayLimit = findExistingDayLimitWithLock(personId, date);
        dayLimit.incrementWalkCount();
    }

    @Override
    @Transactional
    public void decrementDayLimitForPersonAndDate(UUID personId, LocalDate date) {
        log.info("Decrementing day limit for {} at {}", personId, date);
        var dayLimit = findExistingDayLimitWithLock(personId, date);
        dayLimit.decrementWalkCount();
    }

    @Override
    @Transactional
    public int getCurrentWalkCountForAvailableTime(AvailableTime availableTime) {
        if (Hibernate.isInitialized(availableTime.getDayLimit())) {
            return availableTime.getDayLimit().getWalkCount();
        } else {
            return findExistingDayLimitWithLock(availableTime.getPersonId(), availableTime.getDate()).getWalkCount();
        }
    }

    @Override
    @Transactional
    public void decrementDayLimitForAvailableTime(AvailableTime availableTime) {
        DayLimit dayLimit;
        if (Hibernate.isInitialized(availableTime.getDayLimit())) {
            dayLimit = availableTime.getDayLimit();
        } else {
            dayLimit = findExistingDayLimitWithLock(availableTime.getPersonId(), availableTime.getDate());
        }
        dayLimit.decrementWalkCount();
        dayLimitRepository.save(dayLimit);
    }

    private DayLimit findExistingDayLimitWithLock(UUID personId, LocalDate date) {
        return dayLimitRepository
                .findByIdWithLock(new DayLimit.DayLimitId(personId, date))
                .orElseGet(() -> createNewDayLimit(personId, date));
    }

    private DayLimit createNewDayLimit(UUID personId, LocalDate date) {
        DayLimit dayLimit = new DayLimit();
        dayLimit.setDayLimitId(new DayLimit.DayLimitId(personId, date));
        return dayLimitRepository.save(dayLimit);
    }
}
