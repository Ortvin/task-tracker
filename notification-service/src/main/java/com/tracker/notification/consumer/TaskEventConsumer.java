package com.tracker.notification.consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.notification.dto.TaskEvent;
import com.tracker.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskEventConsumer {

    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    @KafkaListener(topics = "task-events", groupId = "notification-service-group")
    public void consume(String message) {
        try {
            log.debug("Raw message: {}", message);

            String json = message
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");

            if (json.startsWith("\"") && json.endsWith("\"")) {
                json = json.substring(1, json.length() - 1);
            }

            TaskEvent event = objectMapper.readValue(json, TaskEvent.class);
            log.info("Received task event: {}", event);
            log.info("Sending email to: {}", event.getUserEmail());

         /*   emailService.sendSimpleMessage(
                    event.getUserEmail(),
                    "Новая задача: " + event.getTitle(),
                    "Создана задача: " + event.getTitle() + "\nОписание: " + event.getDescription());*/

        } catch (Exception e) {
            log.error("Error processing task event", e);
        }
    }
}