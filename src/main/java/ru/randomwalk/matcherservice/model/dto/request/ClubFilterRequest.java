package ru.randomwalk.matcherservice.model.dto.request;

import lombok.Builder;
import ru.randomwalk.matcherservice.model.enam.FilterType;

import java.util.List;
import java.util.UUID;

@Builder
public record ClubFilterRequest(
        FilterType filterType,
        List<UUID> clubsInFilter
) {
}
