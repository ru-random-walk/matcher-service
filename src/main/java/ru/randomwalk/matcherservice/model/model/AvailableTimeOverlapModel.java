package ru.randomwalk.matcherservice.model.model;

import lombok.Builder;
import lombok.ToString;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.UUID;

@Builder
@ToString
public record AvailableTimeOverlapModel (
        LocalDate date,
        OffsetTime timeFrom,
        OffsetTime timeUntil,
        @ToString.Exclude
        AvailableTime initialPersonOverlapAvailableTime,
        @ToString.Exclude
        AvailableTime selectedCandidateAvailableTime
) implements Comparable<AvailableTimeOverlapModel> {

    @Override
    public int compareTo(AvailableTimeOverlapModel o) {
        if (timeFrom.isBefore(o.timeFrom)) {
            return -1;
        } else if (timeFrom.isEqual(o.timeFrom)) {
            return timeUntil.isBefore(o.timeUntil) ? -1 : 1;
        } else {
            return 0;
        }
    }
}
