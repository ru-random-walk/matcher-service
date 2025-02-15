package ru.randomwalk.matcherservice.service;

import ru.randomwalk.matcherservice.model.entity.AvailableTime;

import java.time.OffsetTime;
import java.util.List;
import java.util.UUID;

public interface AvailableTimeService {
     void addAvailableTime(AvailableTime availableTimeToCreate, UUID personId);

     List<AvailableTime> splitAvailableTime(AvailableTime availableTime, OffsetTime splitFrom, OffsetTime splitUntil);

     void decrementDayLimit(AvailableTime availableTime);

     int getCurrentWalkCountWithLock(AvailableTime availableTime);

     AvailableTime getById(UUID id);

     List<AvailableTime> findMatchesForAvailableTime(AvailableTime availableTimeToFindMatches);

}
