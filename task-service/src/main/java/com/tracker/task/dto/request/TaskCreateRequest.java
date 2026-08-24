package com.tracker.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class TaskCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Task type ID is required")
    private Long taskTypeId;

    @NotNull(message = "Plan date is required")
    private LocalDateTime datePlanFinal;

    private List<TaskParticipantRequest> participants;
}