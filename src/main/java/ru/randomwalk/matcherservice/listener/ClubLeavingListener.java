package ru.randomwalk.matcherservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.random.walk.dto.UserExcludeEvent;
import ru.random.walk.topic.EventTopic;
import ru.randomwalk.matcherservice.service.ClubService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubLeavingListener {

    private final ObjectMapper objectMapper;
    private final ClubService clubService;

    @KafkaListener(topics = {EventTopic.USER_EXCLUDE})
    public void processClubLeavingEvent(String message) {
        try {
            log.info("Processing club leave event: {}", message);
            var event = objectMapper.readValue(message, UserExcludeEvent.class);
            clubService.removeUserFromClub(event);
        } catch (Exception e) {
            log.error("Error processing message {} from topic {}", message, EventTopic.USER_EXCLUDE, e);
            throw new RuntimeException(e);
        }
    }
}
