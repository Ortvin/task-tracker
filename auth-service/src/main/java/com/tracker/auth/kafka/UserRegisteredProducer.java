package com.tracker.auth.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.auth.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "user-registered-events";

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, json);
            log.info("Sent user registered event: {}", json);
        } catch (Exception e) {
            log.error("Error sending user registered event", e);
        }
    }
}