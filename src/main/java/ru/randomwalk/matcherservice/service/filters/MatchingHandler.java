package ru.randomwalk.matcherservice.service.filters;

import ru.randomwalk.matcherservice.model.enam.FilterType;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.Person;

import java.util.List;

public interface MatchingHandler {
    boolean supports(FilterType filterType);

    List<AppointmentDetails> getMatches(Person person);
}
