package ru.randomwalk.matcherservice.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.randomwalk.matcherservice.model.enam.FilterType;

import java.util.List;
import java.util.UUID;

@Builder
public record ClubFilterRequest(
        @NotNull
        FilterType filterType,
        @NotNull
        List<UUID> clubsInFilter
) {
}
