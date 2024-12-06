package ru.randomwalk.matcherservice.service;

import ru.randomwalk.matcherservice.model.event.WalkOrganizerStartEvent;

public interface WalkOrganizer {
    void organizeWalk(WalkOrganizerStartEvent event);
}
