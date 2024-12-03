package ru.randomwalk.matcherservice.model.event;


import lombok.Builder;
import ru.randomwalk.matcherservice.service.WalkSearcher;

import java.util.UUID;

/**
 * Starts matching algorithm that pends new appointment or matches current user with existing appointment
 * @see WalkSearcher
 * @param personId  current user id
 */
@Builder
public record WalkSearchStartEvent(
    UUID personId
) { }
