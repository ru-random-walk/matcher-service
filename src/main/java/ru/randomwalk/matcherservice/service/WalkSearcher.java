package ru.randomwalk.matcherservice.service;

import ru.randomwalk.matcherservice.model.event.WalkSearchStartEvent;

public interface WalkSearcher {
    void startWalkSearch(WalkSearchStartEvent event);
}
