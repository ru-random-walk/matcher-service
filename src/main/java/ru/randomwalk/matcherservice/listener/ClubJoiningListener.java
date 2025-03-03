package ru.randomwalk.matcherservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.random.walk.dto.UserJoinEvent;
import ru.random.walk.topic.EventTopic;
import ru.randomwalk.matcherservice.service.ClubService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubJoiningListener {

    private final ObjectMapper objectMapper;
    private final ClubService clubService;

    @KafkaListener(topics = {EventTopic.USER_JOIN})
    public void processClubJoiningEvent(String message) {
        try {
            log.info("Processing club join event: {}", message);
            var event = objectMapper.readValue(message, UserJoinEvent.class);
            clubService.addUserToClub(event);
        } catch (Exception e) {
            log.error("Error processing message {} from topic {}", message, EventTopic.USER_JOIN, e);
            throw new RuntimeException(e);
        }
    }
}
