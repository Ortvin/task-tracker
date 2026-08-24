package com.tracker.task.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskPatchRequest {
    private String title;
    private String description;
    private Long taskTypeId;
    private LocalDateTime datePlanFinal;
    private Long statusId;
}