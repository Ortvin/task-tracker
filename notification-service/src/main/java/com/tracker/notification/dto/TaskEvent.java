package com.tracker.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvent {
    private String eventType;
    private Long taskId;
    private String title;
    private String description;
    private String userEmail;
    private LocalDateTime dateCreated;
    private Long taskTypeId;
    private LocalDateTime datePlanFinal;

}