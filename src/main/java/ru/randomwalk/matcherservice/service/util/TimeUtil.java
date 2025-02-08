package ru.randomwalk.matcherservice.service.util;

import jakarta.annotation.Nullable;
import ru.randomwalk.matcherservice.model.dto.TimePeriod;

import java.time.OffsetTime;

public class TimeUtil {


    @Nullable
    public static TimePeriod getOverlappingInterval(
            TimePeriod firstInterval,
            TimePeriod secondInterval
    ) {
        boolean isOverlapping = isAfterOrEqual(firstInterval.until(), secondInterval.from())
                && isBeforeOrEqual(firstInterval.from(), secondInterval.until());

        if (!isOverlapping) {
            return null;
        }

        OffsetTime maxStart = isAfterOrEqual(firstInterval.from(), secondInterval.from())
                ? firstInterval.from() : secondInterval.from();
        OffsetTime minEnd = isBeforeOrEqual(firstInterval.until(), secondInterval.until())
                ? firstInterval.until() : secondInterval.until();

        return TimePeriod.of(maxStart, minEnd);
    }

    public static boolean isAfterOrEqual(OffsetTime first, OffsetTime second) {
        return first.isAfter(second) || first.equals(second);
    }

    public static boolean isBeforeOrEqual(OffsetTime first, OffsetTime second) {
        return first.isBefore(second) || first.equals(second);
    }
}
