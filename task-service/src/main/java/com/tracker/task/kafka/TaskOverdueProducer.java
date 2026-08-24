package com.tracker.task.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tracker.task.event.TaskOverdueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskOverdueProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "task-overdue-events";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public void sendOverdueEvent(TaskOverdueEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, json);
            log.info("Sent overdue event to Kafka: {}", json);
        } catch (JsonProcessingException e) {
            log.error("Error serializing overdue event", e);
        }
    }
}