package ru.randomwalk.matcherservice.service.util;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;

public class TimeUtil {


    @Nullable
    public static Pair<OffsetTime, OffsetTime> getOverlappingInterval(
            Pair<OffsetTime, OffsetTime> firstInterval,
            Pair<OffsetTime, OffsetTime> secondInterval
    ) {
        boolean isOverlapping = isAfterOrEqual(firstInterval.getRight(), secondInterval.getLeft())
                && isBeforeOrEqual(firstInterval.getLeft(), secondInterval.getRight());

        if (!isOverlapping) {
            return null;
        }

        OffsetTime maxStart = isAfterOrEqual(firstInterval.getLeft(), secondInterval.getLeft())
                ? firstInterval.getLeft() : secondInterval.getLeft();
        OffsetTime minEnd = isBeforeOrEqual(firstInterval.getRight(), secondInterval.getRight())
                ? firstInterval.getRight() : secondInterval.getRight();

        return Pair.of(maxStart, minEnd);
    }

    public static boolean isAfterOrEqual(OffsetTime first, OffsetTime second) {
        return first.isAfter(second) || first.equals(second);
    }

    public static boolean isBeforeOrEqual(OffsetTime first, OffsetTime second) {
        return first.isBefore(second) || first.equals(second);
    }

    public static boolean isDifferenceWithinIntervalExist(Pair<OffsetTime, OffsetTime> interval, int differenceInSeconds) {
        return ChronoUnit.SECONDS.between(interval.getLeft(), interval.getRight()) >= differenceInSeconds;
    }
}
