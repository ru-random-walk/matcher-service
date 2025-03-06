package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.random.walk.dto.CreatePrivateChatEvent;
import ru.random.walk.topic.EventTopic;
import ru.randomwalk.matcherservice.model.event.CreateChatsWithPartnersEvent;
import ru.randomwalk.matcherservice.service.ChatCreationService;
import ru.randomwalk.matcherservice.service.KafkaService;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatCreationServiceImpl implements ChatCreationService {

    private final KafkaService kafkaService;

    @Override
    @TransactionalEventListener
    public void createChatsWithPartners(CreateChatsWithPartnersEvent event) {
        UUID personId = event.initialPersonId();
        List<UUID> partnersIds = event.partnerIds();
        log.info("Sending create chat events between person {} and partners {}", personId, partnersIds);
        for (var partnerId : partnersIds) {
            kafkaService.sendMessage(EventTopic.CREATE_CHAT, new CreatePrivateChatEvent(personId, partnerId));
        }
    }
}
