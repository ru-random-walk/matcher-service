package ru.randomwalk.matcherservice.service;

public interface OutboxSenderService {
    void sendMessage(String topic, Object payload);
}
