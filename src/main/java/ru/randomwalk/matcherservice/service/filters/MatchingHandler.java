package ru.randomwalk.matcherservice.service.filters;

import ru.randomwalk.matcherservice.model.enam.FilterType;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.model.model.MatchingResultModel;

import java.util.List;

public interface MatchingHandler {
    boolean supports(FilterType filterType);

    List<MatchingResultModel> getMatches(Person person);
}
