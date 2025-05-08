package ru.randomwalk.matcherservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.model.entity.OutboxMessage;
import ru.randomwalk.matcherservice.repository.OutboxRepository;
import ru.randomwalk.matcherservice.service.OutboxSenderService;
import ru.randomwalk.matcherservice.service.util.VirtualThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxSenderServiceImpl implements OutboxSenderService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void sendMessage(String topic, Object payload) {
        var message = getMessage(topic, serializePayload(payload));
        outboxRepository.save(message);
    }

    @Override
    @Transactional
    public void sendBatchOfMessages(String topic, List<?> payloads) {
        List<Object> objects = new ArrayList<>(payloads);
        List<String> messagePayloads = VirtualThreadUtil.getResultsConcurrently(objects, this::serializePayload);
        var messages = messagePayloads.stream()
                .map(messagePayload -> getMessage(topic, messagePayload))
                .collect(Collectors.toList());
        outboxRepository.saveAll(messages);
    }

    private OutboxMessage getMessage(String topic, String payload) {
        var message = new OutboxMessage();
        message.setTopic(topic);
        message.setPayload(payload);
        return message;
    }

    private String serializePayload(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error serializing object {} for payload", object, e);
            throw new RuntimeException(e);
        }
    }

}
