package ru.randomwalk.matcherservice.service;

public interface KafkaService {
    void sendMessage(String topic, Object messageObject);
}
