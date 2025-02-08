package ru.randomwalk.matcherservice.model.dto;

import java.time.OffsetTime;

public record TimePeriod(OffsetTime from, OffsetTime until) {
    public static TimePeriod of(OffsetTime from, OffsetTime until) {
        return new TimePeriod(from, until);
    }
}
