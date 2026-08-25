package com.tracker.task.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private String taskTypeName;
    private TaskStatusDto status;
    private LocalDateTime dateCreated;
    private LocalDateTime datePlanFinal;
    private LocalDateTime dateFactFinal;
    private List<TaskParticipantResponse> participants;
}