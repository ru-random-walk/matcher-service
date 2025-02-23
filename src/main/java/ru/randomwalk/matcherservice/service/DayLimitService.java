package ru.randomwalk.matcherservice.service;

import ru.randomwalk.matcherservice.model.entity.AvailableTime;

import java.time.LocalDate;
import java.util.UUID;

public interface DayLimitService {
    void incrementDayLimitForPersonAndDate(UUID personId, LocalDate date);
    int getCurrentWalkCountForAvailableTime(AvailableTime availableTime);
    void decrementDayLimitForAvailableTime(AvailableTime availableTime);
}
