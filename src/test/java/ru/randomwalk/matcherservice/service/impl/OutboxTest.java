package ru.randomwalk.matcherservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.random.walk.dto.CreatePrivateChatEvent;
import ru.random.walk.topic.EventTopic;
import ru.randomwalk.matcherservice.AbstractContainerTest;
import ru.randomwalk.matcherservice.model.entity.OutboxMessage;
import ru.randomwalk.matcherservice.repository.OutboxRepository;
import ru.randomwalk.matcherservice.service.OutboxSenderService;
import ru.randomwalk.matcherservice.service.job.OutboxSendingJob;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
class OutboxTest extends AbstractContainerTest {

    @Autowired
    private OutboxSenderService outboxSenderService;

    @Autowired
    private OutboxSendingJob outboxSendingJob;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    @Rollback
    void sendMessageSavesToDb() {
        UUID personId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();
        var event = new CreatePrivateChatEvent(personId, partnerId);
        outboxSenderService.sendMessage(EventTopic.CREATE_CHAT, event);
        var messages = outboxRepository.findAll();

        assertFalse(messages.isEmpty());
        assertEquals(EventTopic.CREATE_CHAT, messages.getLast().getTopic());
        assertEquals(getPayload(event), messages.getLast().getPayload());
        assertNotNull(messages.getLast().getCreatedAt());
    }

    @Test
    @Transactional
    @Rollback
    void sendBatchOfMessagesSavesToDb() {
        var events = List.of(
                new CreatePrivateChatEvent(UUID.randomUUID(), UUID.randomUUID()),
                new CreatePrivateChatEvent(UUID.randomUUID(), UUID.randomUUID()),
                new CreatePrivateChatEvent(UUID.randomUUID(), UUID.randomUUID()),
                new CreatePrivateChatEvent(UUID.randomUUID(), UUID.randomUUID())
        );
        outboxSenderService.sendBatchOfMessages(EventTopic.CREATE_CHAT, events);
        var messages = outboxRepository.findAll();

        assertEquals(events.size(), messages.size());
        assertTrue(messages.stream().allMatch(message -> message.getTopic().equals(EventTopic.CREATE_CHAT)));
        assertTrue(messages.stream().allMatch(message -> message.getCreatedAt() != null));
        assertTrue(messages.stream().allMatch(message -> Strings.isNotBlank(message.getPayload())));
        for (var event : events) {
            assertTrue(messages.stream().anyMatch(message -> message.getPayload().equals(getPayload(event))));
        }
    }

    @Test
    @Transactional
    @Rollback
    void checkOutboxJobIsSendingMessages() {
        String payload = getPayload(new CreatePrivateChatEvent(UUID.randomUUID(), UUID.randomUUID()));

        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setPayload(payload);
        outboxMessage.setTopic(EventTopic.CREATE_CHAT);

        assertFalse(outboxMessage.isSent());

        outboxMessage = outboxRepository.save(outboxMessage);

        outboxSendingJob.execute(null);

        var outboxResult = outboxRepository.findById(outboxMessage.getId()).get();
        assertTrue(outboxResult.isSent());
    }

    private String getPayload(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}