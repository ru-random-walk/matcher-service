package ru.randomwalk.matcherservice.service;

import java.util.List;

public interface OutboxSenderService {
    void sendMessage(String topic, Object payload);
    void sendBatchOfMessages(String topic, List<?> payloads);
}
