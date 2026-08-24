package com.tracker.task.mapper;

import com.tracker.task.dto.request.TaskCreateRequest;
import com.tracker.task.dto.response.TaskParticipantResponse;
import com.tracker.task.dto.response.TaskResponse;
import com.tracker.task.dto.response.TaskStatusDto;
import com.tracker.task.model.*;
import com.tracker.task.repository.ParticipantRepository;
import com.tracker.task.repository.ParticipantRoleRepository;
import com.tracker.task.repository.TaskStatusRepository;
import com.tracker.task.repository.TaskTypeRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final TaskTypeRepository taskTypeRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantRoleRepository roleRepository;

    public Task toEntity(TaskCreateRequest request) {
        TaskType taskType = taskTypeRepository.findById(request.getTaskTypeId())
                .orElseThrow(() -> new IllegalArgumentException("TaskType not found"));

        TaskStatus status = taskStatusRepository.findByCode("PENDING")
                .orElseThrow(() -> new IllegalArgumentException("Status PENDING not found"));

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .taskType(taskType)
                .status(status)
                .datePlanFinal(request.getDatePlanFinal())
                .build();

        if (request.getParticipants() != null) {
            request.getParticipants().forEach(p -> {
                Participant participant = participantRepository.findById(p.getParticipantId())
                        .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
                ParticipantRole role = roleRepository.findById(p.getRoleId())
                        .orElseThrow(() -> new IllegalArgumentException("Role not found"));

                TaskParticipant taskParticipant = TaskParticipant.builder()
                        .task(task)
                        .participant(participant)
                        .role(role)
                        .build();

                task.getParticipants().add(taskParticipant);
            });
        }

        return task;
    }

    public TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .taskTypeName(task.getTaskType().getName())
                .status(TaskStatusDto.builder()
                        .id(task.getStatus().getId())
                        .code(task.getStatus().getCode())
                        .name(task.getStatus().getName())
                        .build())
                .dateCreated(task.getDateCreated())
                .datePlanFinal(task.getDatePlanFinal())
                .dateFactFinal(task.getDateFactFinal())
                .participants(task.getParticipants().stream()
                        .map(tp -> TaskParticipantResponse.builder()
                                .participantId(tp.getParticipant().getId())
                                .participantName(getParticipantName(tp.getParticipant()))
                                .roleName(tp.getRole().getName())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private Participant unwrapParticipant(Participant participant) {
        if (participant instanceof HibernateProxy) {
            return (Participant) ((HibernateProxy) participant).getHibernateLazyInitializer().getImplementation();
        }
        return participant;
    }

    private String getParticipantName(Participant participant) {
        Participant unwrapped = unwrapParticipant(participant);

        if (unwrapped instanceof Person) {
            Person p = (Person) unwrapped;
            String middle = p.getMiddleName() != null ? " " + p.getMiddleName() : "";
            return p.getLastName() + " " + p.getName() + middle;
        } else if (unwrapped instanceof Organisation) {
            return ((Organisation) unwrapped).getShortName();
        }
        return "Unknown";
    }
}