package ru.randomwalk.matcherservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.random.walk.kafka.EventTopic;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.UserRegistrationListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationListenerImpl implements UserRegistrationListener {

    private final ObjectMapper objectMapper;
    private final PersonService personService;

    @Override
    @KafkaListener(topics = EventTopic.USER_REGISTRATION)
    public void process(String message) {
        try {
            log.info("Processing message {} from topic {}", message, EventTopic.USER_REGISTRATION);

            RegisteredUserInfoEvent userInfoEvent = objectMapper.readValue(message, RegisteredUserInfoEvent.class);
            personService.addNewPerson(userInfoEvent);

            log.info("Message {} is processed", message);
        } catch (Exception e) {
            log.error("Error processing message {} from topic {}", message, EventTopic.USER_REGISTRATION, e);
            throw new RuntimeException(e);
        }
    }
}
