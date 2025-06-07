package ru.randomwalk.matcherservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.random.walk.topic.EventTopic;
import ru.randomwalk.matcherservice.service.PersonService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationListener {

    private final ObjectMapper objectMapper;
    private final PersonService personService;

    @KafkaListener(topics = EventTopic.USER_REGISTRATION)
    public void process(String message) {
        try {
            log.info("Processing message {} from topic {}", message, EventTopic.USER_REGISTRATION);

            RegisteredUserInfoEvent userInfoEvent = objectMapper.readValue(message, RegisteredUserInfoEvent.class);
            personService.addNewOrUpdateExistingPerson(userInfoEvent);

            log.info("Message {} is processed", message);
        } catch (Exception e) {
            log.error("Error processing message {} from topic {}", message, EventTopic.USER_REGISTRATION, e);
            throw new RuntimeException(e);
        }
    }
}
