package com.tracker.task.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.task.dto.UserRegisteredEvent;
import com.tracker.task.service.PersonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final ObjectMapper objectMapper;
    private final PersonService personService;

    @KafkaListener(topics = "user-registered-events", groupId = "task-service-group")
    public void consume(String message) {
        try {
            log.info("Received user registered event: {}", message);
            UserRegisteredEvent event = objectMapper.readValue(message, UserRegisteredEvent.class);

            personService.createPersonFromRegistration(
                    event.getUserId(),
                    event.getFirstName(),
                    event.getMiddleName(),
                    event.getLastName(),
                    event.getEmail()
            );

            log.info("Person created for user: {}", event.getUserId());

        } catch (Exception e) {
            log.error("Error processing user registered event", e);
        }
    }
}