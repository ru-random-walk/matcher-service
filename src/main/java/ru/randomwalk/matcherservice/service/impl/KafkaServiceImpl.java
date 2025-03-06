package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.service.KafkaService;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaServiceImpl implements KafkaService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendMessage(String topic, Object messageObject) {
        try {
            log.info("Sending message to topic: {}. Content: {}", topic, messageObject);
            kafkaTemplate.send(topic, messageObject);
        } catch (Exception e) {
            log.error("Error while sending message to topic {}", topic, e);
        }
    }

}
