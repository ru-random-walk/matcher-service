package ru.randomwalk.matcherservice.model.model;

import lombok.Builder;
import ru.randomwalk.matcherservice.model.entity.Person;

import java.util.List;

@Builder
public record MatchingResultModel (
        Person matchedPartner,
        List<AvailableTimeOverlapModel> availableTimeOverlapModels
) {}
