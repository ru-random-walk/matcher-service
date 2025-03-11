package ru.randomwalk.matcherservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    void sendMessageSavesToDb() throws JsonProcessingException {
        UUID personId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();
        outboxSenderService.sendMessage(EventTopic.CREATE_CHAT, new CreatePrivateChatEvent(personId, partnerId));
        var messages = outboxRepository.findAll();

        assertFalse(messages.isEmpty());
        assertEquals(EventTopic.CREATE_CHAT, messages.getLast().getTopic());
        assertEquals(getPayload(personId, partnerId), messages.getLast().getPayload());
        assertNotNull(messages.getLast().getCreatedAt());
    }

    @Test
    @Transactional
    @Rollback
    void checkOutboxJobIsSendingMessages() throws JsonProcessingException {
        String payload = getPayload(UUID.randomUUID(), UUID.randomUUID());

        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setPayload(payload);
        outboxMessage.setTopic(EventTopic.CREATE_CHAT);

        assertFalse(outboxMessage.isSent());

        outboxMessage = outboxRepository.save(outboxMessage);

        outboxSendingJob.execute(null);

        var outboxResult = outboxRepository.findById(outboxMessage.getId()).get();
        assertTrue(outboxResult.isSent());
    }

    private String getPayload(UUID member1, UUID member2) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new CreatePrivateChatEvent(member1, member2));
    }
}