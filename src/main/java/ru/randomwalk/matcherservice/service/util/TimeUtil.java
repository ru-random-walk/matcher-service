package ru.randomwalk.matcherservice.service.util;

import com.nimbusds.jose.util.Pair;
import lombok.experimental.UtilityClass;

import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;

@UtilityClass
public class TimeUtil {


    public Pair<OffsetTime, OffsetTime> getOverlappingInterval(
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

    public boolean isAfterOrEqual(OffsetTime first, OffsetTime second) {
        return first.isAfter(second) || first.equals(second);
    }

    public boolean isBeforeOrEqual(OffsetTime first, OffsetTime second) {
        return first.isAfter(second) || first.equals(second);
    }

    public boolean isDifferenceWithinIntervalExist(Pair<OffsetTime, OffsetTime> interval, int differenceInSeconds) {
        return ChronoUnit.SECONDS.between(interval.getLeft(), interval.getRight()) >= differenceInSeconds;
    }
}
