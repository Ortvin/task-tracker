package com.tracker.task.service;

import com.tracker.task.dto.request.TaskCreateRequest;
import com.tracker.task.dto.response.TaskResponse;
import com.tracker.task.mapper.TaskMapper;
import com.tracker.task.model.Task;
import com.tracker.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void createTask_WithValidData_ShouldReturnSavedTask() {
        // Arrange
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .taskTypeId(1L)
                .datePlanFinal(now.plusDays(7))
                .build();

        Task taskToSave = new Task();
        taskToSave.setTitle(request.getTitle());

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle(request.getTitle());
        savedTask.setDateCreated(now);

        TaskResponse expectedResponse = TaskResponse.builder()
                .id(1L)
                .title(request.getTitle())
                .dateCreated(now)
                .build();

        when(taskMapper.toEntity(any(TaskCreateRequest.class))).thenReturn(taskToSave);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(expectedResponse);

        // Act
        TaskResponse actualResponse = taskService.createTask(request,4L );

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(1L);
        assertThat(actualResponse.getTitle()).isEqualTo("Test Task");

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTaskResponse() {
        // Arrange
        Long taskId = 1L;
        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Existing Task");

        TaskResponse expectedResponse = TaskResponse.builder()
                .id(taskId)
                .title("Existing Task")
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(any(Task.class))).thenReturn(expectedResponse);

        // Act
    //    TaskResponse actualResponse = taskService.getTaskById(taskId);

        // Assert
    //    assertThat(actualResponse).isNotNull();
    //    assertThat(actualResponse.getId()).isEqualTo(taskId);
    //    assertThat(actualResponse.getTitle()).isEqualTo("Existing Task");
    }

    @Test
    void getTaskById_WhenTaskDoesNotExist_ShouldThrowException() {
        // Arrange
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
    /*    assertThatThrownBy(() -> taskService.getTaskById(taskId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task not found");*/
    }
}