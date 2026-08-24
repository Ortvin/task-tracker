package com.tracker.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.notification.dto.TaskOverdueEvent;
import com.tracker.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskOverdueEventConsumer {

    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    @KafkaListener(topics = "task-overdue-events", groupId = "notification-service-group")
    public void consume(String message) {
        try {
            log.debug("Raw overdue message: {}", message);

            // Убираем экранирование
            String json = message
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");

            if (json.startsWith("\"") && json.endsWith("\"")) {
                json = json.substring(1, json.length() - 1);
            }

            TaskOverdueEvent event = objectMapper.readValue(json, TaskOverdueEvent.class);
            log.info("Received overdue event: {}", event);
            log.info("Sending overdue notification to: {}", event.getUserEmail());

        /*    emailService.sendSimpleMessage(
                    event.getUserEmail(),
                    "Задача просрочена: " + event.getTitle(),
                    "Задача " + event.getTitle() + " просрочена. Дедлайн: " + event.getDeadline());*/

        } catch (Exception e) {
            log.error("Error processing overdue event", e);
        }
    }
}