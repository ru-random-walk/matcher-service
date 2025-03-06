package ru.randomwalk.matcherservice.model.event;

import java.util.List;
import java.util.UUID;

public record CreateChatsWithPartnersEvent(
        UUID initialPersonId,
        List<UUID> partnerIds
) { }
