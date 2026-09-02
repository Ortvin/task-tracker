package com.tracker.task.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusHistoryResponse {
    private Long id;
    private Long taskId;
    private String statusCode;
    private String statusName;
    private String changedBy;
    private LocalDateTime changedAt;
}