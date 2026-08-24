package com.tracker.task.service;

import com.tracker.task.dto.request.TaskCreateRequest;
import com.tracker.task.dto.request.TaskPatchRequest;
import com.tracker.task.dto.response.TaskResponse;
import com.tracker.task.event.TaskEvent;
import com.tracker.task.kafka.TaskEventProducer;
import com.tracker.task.mapper.TaskMapper;
import com.tracker.task.model.*;
import com.tracker.task.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskTypeRepository taskTypeRepository;
    private final TaskMapper taskMapper;
    private final TaskEventProducer taskEventProducer;
    private final TaskStatusRepository taskStatusRepository;
    private final UserContactService userContactService;
    private final PersonRepository personRepository;
    private final ParticipantRoleRepository participantRoleRepository;
    private final TaskParticipantRepository taskParticipantRepository;

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request, Long userId) {
        Task task = taskMapper.toEntity(request);
        Task saved = taskRepository.save(task);

        // Добавляем автора как участника
        Person author = personRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Person not found for userId: " + userId));

        ParticipantRole authorRole = participantRoleRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Role 'Создатель' not found"));

        TaskParticipant taskParticipant = TaskParticipant.builder()
                .task(task)
                .participant(author)
                .role(authorRole)
                .build();

        task.getParticipants().add(taskParticipant);
        // Отправляем email в Kafka
        String email = userContactService.getPrimaryEmail(userId);
        TaskEvent event = TaskEvent.builder()
                .eventType("CREATED")
                .taskId(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .taskTypeId(saved.getTaskType().getId())
                .datePlanFinal(saved.getDatePlanFinal())
                .dateCreated(saved.getDateCreated())
                .userEmail(email)
                .build();

        taskEventProducer.sendTaskCreatedEvent(event);

        return taskMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id, Long userId, String role) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!"ADMIN".equals(role) && !isParticipant(id, userId)) {
            throw new AccessDeniedException("Access denied");
        }

        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskCreateRequest request, Long userId, String role) {
        if (!canUpdateTask(id, userId, role)) {
            throw new AccessDeniedException("You don't have permission to update this task");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // Обновляем поля
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setTaskType(taskTypeRepository.findById(request.getTaskTypeId())
                .orElseThrow(() -> new IllegalArgumentException("TaskType not found")));
        task.setDatePlanFinal(request.getDatePlanFinal());

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new IllegalArgumentException("Task not found");
        }
        taskRepository.deleteById(id);
    }

    @Transactional
    public TaskResponse patchTask(Long id, TaskPatchRequest request, Long userId, String role) {
        if (!canPatchTask(id, userId, role)) {
            throw new AccessDeniedException("You don't have permission to patch this task");
        }
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getTaskTypeId() != null) {
            TaskType taskType = taskTypeRepository.findById(request.getTaskTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("TaskType not found"));
            task.setTaskType(taskType);
        }
        if (request.getDatePlanFinal() != null) {
            task.setDatePlanFinal(request.getDatePlanFinal());
        }
        if (request.getStatusId() != null) {
            TaskStatus status = taskStatusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new IllegalArgumentException("Status not found"));
            task.setStatus(status);
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<Task> getFilteredTasks(Long statusId, LocalDate fromDate, LocalDate toDate,
                                       Pageable pageable, Long userId, String role) {
        // ADMIN — видит всё
        if ("ADMIN".equalsIgnoreCase(role)) {
            if (statusId != null && fromDate != null && toDate != null) {
                return taskRepository.findByStatusIdAndDatePlanFinalBetween(
                        statusId,
                        fromDate.atStartOfDay(),
                        toDate.atStartOfDay().plusDays(1),
                        pageable
                );
            } else if (statusId != null) {
                return taskRepository.findByStatusId(statusId, pageable);
            } else if (fromDate != null && toDate != null) {
                return taskRepository.findByDatePlanFinalBetween(
                        fromDate.atStartOfDay(),
                        toDate.atStartOfDay().plusDays(1),
                        pageable
                );
            } else {
                return taskRepository.findAll(pageable);
            }
        }

        // USER — только свои задачи (через TaskParticipant)
        if (statusId != null && fromDate != null && toDate != null) {
            return taskRepository.findTasksByUserIdAndStatusIdAndDatePlanFinalBetween(
                    userId, statusId, fromDate.atStartOfDay(), toDate.atStartOfDay().plusDays(1), pageable
            );
        } else if (statusId != null) {
            return taskRepository.findTasksByUserIdAndStatusId(
                    userId, statusId, pageable
            );
        } else if (fromDate != null && toDate != null) {
            return taskRepository.findTasksByUserIdAndDatePlanFinalBetween(
                    userId, fromDate.atStartOfDay(), toDate.atStartOfDay().plusDays(1), pageable
            );
        } else {
            return taskRepository.findTasksByUserId(userId, pageable);
        }
    }

    private boolean isParticipant(Long taskId, Long userId) {
        return taskParticipantRepository.isUserParticipant(taskId, userId);
    }

    private Optional<ParticipantRole> getUserRoleInTask(Long taskId, Long userId) {
        return taskParticipantRepository.findRoleByTaskIdAndUserId(taskId, userId);
    }

    private boolean canUpdateTask(Long taskId, Long userId, String role) {
        if ("ADMIN".equals(role)) {
            return true;
        }

        ParticipantRole userRole = getUserRoleInTask(taskId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a participant of this task"));

        String roleName = userRole.getName();
        return "Создатель".equals(roleName) || "Исполнитель".equals(roleName);
    }

    private boolean canPatchTask(Long taskId, Long userId, String role) {
        if ("ADMIN".equals(role)) {
            return true;
        }

        ParticipantRole userRole = getUserRoleInTask(taskId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a participant of this task"));

        String roleName = userRole.getName();

        return "Создатель".equals(roleName) || "Исполнитель".equals(roleName);
    }

}