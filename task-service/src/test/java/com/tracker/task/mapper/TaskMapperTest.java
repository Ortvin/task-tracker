package com.tracker.task.mapper;

import com.tracker.task.dto.request.TaskCreateRequest;
import com.tracker.task.dto.response.TaskResponse;
import com.tracker.task.model.*;
import com.tracker.task.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskMapperTest {

    @Mock
    private TaskTypeRepository taskTypeRepository;

    @Mock
    private TaskStatusRepository taskStatusRepository;

    @InjectMocks
    private TaskMapper taskMapper;

    private TaskType taskType;
    private TaskStatus status;
    private TaskCreateRequest request;

    @BeforeEach
    void setUp() {
        taskType = new TaskType();
        taskType.setId(1L);
        taskType.setName("Epic");

        status = new TaskStatus();
        status.setId(1L);
        status.setCode("PENDING");
        status.setName("Ожидает");

        request = TaskCreateRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .taskTypeId(1L)
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    void toEntity_ShouldMapRequestToTask() {
        when(taskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));
        when(taskStatusRepository.findByCode("PENDING")).thenReturn(Optional.of(status));

        Task task = taskMapper.toEntity(request);

        assertThat(task).isNotNull();
        assertThat(task.getTitle()).isEqualTo("Test Task");
        assertThat(task.getDescription()).isEqualTo("Test Description");
        assertThat(task.getTaskType()).isEqualTo(taskType);
        assertThat(task.getStatus()).isEqualTo(status);
    }

    @Test
    void toResponse_ShouldMapTaskToResponse() {
        Task task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .taskType(taskType)
                .status(status)
                .dateCreated(LocalDateTime.now())
                .build();

        TaskResponse response = taskMapper.toResponse(task);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Task");
        assertThat(response.getStatus()).isNotNull();
        assertThat(response.getStatus().getCode()).isEqualTo("PENDING");
    }
}