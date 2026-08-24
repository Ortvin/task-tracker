package com.tracker.task.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskParticipantResponse {

    private Long participantId;
    private String participantName;
    private String roleName;
}