package com.tracker.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tracker.notification.dto.TaskEvent;
import com.tracker.notification.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskEventConsumerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TaskEventConsumer consumer;

    @Test
    void consume_ShouldCallEmailService() throws Exception {
        String json = """
                {
                    "eventType": "CREATED",
                    "taskId": 1,
                    "title": "Test Task",
                    "description": "Test Description",
                    "userEmail": "test@example.com",
                    "dateCreated": "2026-08-25T18:00:00"
                }
                """;

        String cleaned = json.replace("\\\"", "\"");
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        // Создаём реальный ObjectMapper для парсинга
        ObjectMapper realMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        when(objectMapper.readValue(anyString(), eq(TaskEvent.class)))
                .thenReturn(realMapper.readValue(cleaned, TaskEvent.class));

        consumer.consume(cleaned);

        verify(emailService, times(1)).sendSimpleMessage(
                eq("test@example.com"),
                anyString(),
                anyString()
        );
    }

    @Test
    void consume_ShouldHandleInvalidJson() throws Exception {
        when(objectMapper.readValue(anyString(), eq(TaskEvent.class)))
                .thenThrow(new RuntimeException("Invalid JSON"));

        consumer.consume("invalid json");

        verify(emailService, never()).sendSimpleMessage(any(), any(), any());
    }
}