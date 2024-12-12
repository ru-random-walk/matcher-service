package ru.randomwalk.matcherservice.model.model;

import lombok.Builder;
import lombok.ToString;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;

import java.time.LocalDate;
import java.time.OffsetTime;

@Builder
public record AvailableTimeOverlapModel (
        LocalDate date,
        OffsetTime timeFrom,
        OffsetTime timeUntil,
        @ToString.Exclude
        AvailableTime firstOverlappingAvailableTime,
        @ToString.Exclude
        AvailableTime secondOverlappingAvailableTime
){}
