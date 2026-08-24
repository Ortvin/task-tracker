package com.tracker.task.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskOverdueEvent {
    private Long taskId;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private String userEmail;
}