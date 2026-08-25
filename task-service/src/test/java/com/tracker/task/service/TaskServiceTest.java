package com.tracker.task.service;

import com.tracker.task.dto.request.TaskCreateRequest;
import com.tracker.task.dto.request.TaskPatchRequest;
import com.tracker.task.dto.response.TaskResponse;
import com.tracker.task.mapper.TaskMapper;
import com.tracker.task.model.*;
import com.tracker.task.repository.*;
import com.tracker.task.kafka.TaskEventProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private ParticipantRoleRepository participantRoleRepository;

    @Mock
    private TaskParticipantRepository taskParticipantRepository;

    @Mock
    private TaskTypeRepository taskTypeRepository;

    @Mock
    private TaskStatusRepository taskStatusRepository;

    @Mock
    private UserContactService userContactService;

    @Mock
    private TaskEventProducer taskEventProducer;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_ShouldCreateTaskAndReturnResponse() {
        // Arrange
        Long userId = 1L;
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Test Task");

        TaskType taskType = new TaskType();
        taskType.setId(1L);
        taskType.setName("Epic");

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setTaskType(taskType);

        Person author = new Person();
        author.setUserId(userId);

        ParticipantRole authorRole = new ParticipantRole();
        authorRole.setId(1L);
        authorRole.setName("Создатель");

        TaskResponse response = new TaskResponse();
        response.setId(1L);
        response.setTitle("Test Task");

        when(taskMapper.toEntity(any())).thenReturn(task);
        when(taskRepository.save(any())).thenReturn(task);
        when(personRepository.findByUserId(userId)).thenReturn(Optional.of(author));
        when(participantRoleRepository.findById(1L)).thenReturn(Optional.of(authorRole));
        when(userContactService.getPrimaryEmail(userId)).thenReturn("test@example.com");
        when(taskMapper.toResponse(any())).thenReturn(response);

        // Act
        TaskResponse result = taskService.createTask(request, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Task");

        verify(taskRepository).save(any());
        verify(taskEventProducer).sendTaskCreatedEvent(any());
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenExists() {
        Long taskId = 1L;
        Long userId = 1L;
        String role = "USER";

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");

        TaskResponse response = new TaskResponse();
        response.setId(taskId);
        response.setTitle("Test Task");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskParticipantRepository.isUserParticipant(taskId, userId)).thenReturn(true);
        when(taskMapper.toResponse(task)).thenReturn(response);

        TaskResponse result = taskService.getTaskById(taskId, userId, role);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(taskId);
        assertThat(result.getTitle()).isEqualTo("Test Task");
    }

    @Test
    void getTaskById_ShouldThrowException_WhenTaskNotFound() {
        Long taskId = 999L;
        Long userId = 1L;
        String role = "USER";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(taskId, userId, role))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void updateTask_ShouldUpdateTask_WhenUserIsAdmin() {
        Long taskId = 1L;
        Long userId = 1L;
        String role = "ADMIN";

        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Updated Title");
        request.setTaskTypeId(1L);

        TaskType taskType = new TaskType();
        taskType.setId(1L);
        taskType.setName("Epic");

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle("Old Title");
        existingTask.setTaskType(taskType);

        Task updatedTask = new Task();
        updatedTask.setId(taskId);
        updatedTask.setTitle("Updated Title");
        updatedTask.setTaskType(taskType);

        TaskResponse response = new TaskResponse();
        response.setId(taskId);
        response.setTitle("Updated Title");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));
        when(taskRepository.save(any())).thenReturn(updatedTask);
        when(taskMapper.toResponse(any())).thenReturn(response);

        TaskResponse result = taskService.updateTask(taskId, request, userId, role);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void updateTask_ShouldThrowAccessDenied_WhenUserNotParticipant() {
        Long taskId = 1L;
        Long userId = 2L;
        String role = "USER";

        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Updated Title");

        Task existingTask = new Task();
        existingTask.setId(taskId);

    //    when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        assertThatThrownBy(() -> taskService.updateTask(taskId, request, userId, role))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not a participant");
    }

    @Test
    void patchTask_ShouldUpdateStatus_WhenUserIsExecutor() {
        Long taskId = 1L;
        Long userId = 2L;
        String role = "USER";

        TaskPatchRequest request = new TaskPatchRequest();
        request.setStatusId(3L);

        TaskStatus status = new TaskStatus();
        status.setId(3L);
        status.setCode("COMPLETED");

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle("Old Title");

        Task updatedTask = new Task();
        updatedTask.setId(taskId);
        updatedTask.setTitle("Old Title");
        updatedTask.setStatus(status);

        TaskResponse response = new TaskResponse();
        response.setId(taskId);
        response.setTitle("Old Title");

        ParticipantRole userRole = new ParticipantRole();
        userRole.setId(2L);
        userRole.setName("Исполнитель");

        when(taskParticipantRepository.findRoleByTaskIdAndUserId(taskId, userId))
                .thenReturn(Optional.of(userRole));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskStatusRepository.findById(3L)).thenReturn(Optional.of(status));
        when(taskRepository.save(any())).thenReturn(updatedTask);
        when(taskMapper.toResponse(any())).thenReturn(response);

        TaskResponse result = taskService.patchTask(taskId, request, userId, role);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(taskId);
    }

    @Test
    void deleteTask_ShouldDelete_WhenTaskExists() {
        Long taskId = 1L;

        when(taskRepository.existsById(taskId)).thenReturn(true);

        taskService.deleteTask(taskId);

        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void deleteTask_ShouldThrowException_WhenTaskNotFound() {
        Long taskId = 999L;

        when(taskRepository.existsById(taskId)).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask(taskId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task not found");
    }
}