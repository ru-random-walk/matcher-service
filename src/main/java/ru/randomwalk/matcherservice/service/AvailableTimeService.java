package ru.randomwalk.matcherservice.service;

import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.DayLimit;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.model.AvailableTimeOverlapModel;

import java.time.OffsetTime;
import java.util.List;
import java.util.UUID;

public interface AvailableTimeService {
     void addAvailableTime(List<AvailableTime> newAvailableTimes, UUID personId);

     void removeAvailableTimeForPerson(AvailableTime availableTime, Person person);

     List<AvailableTimeOverlapModel> getAllAvailableTimeOverlaps(List<AvailableTime> first, List<AvailableTime> second);

     List<AvailableTime> splitAvailableTime(AvailableTime availableTime, OffsetTime splitFrom, OffsetTime splitUntil);

     void decrementDayLimit(DayLimit dayLimit);

}
