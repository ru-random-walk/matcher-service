package ru.randomwalk.matcherservice.service;

import ru.randomwalk.matcherservice.model.event.CreateChatsWithPartnersEvent;

public interface ChatCreationService {
    void createChatsWithPartners(CreateChatsWithPartnersEvent event);
}
