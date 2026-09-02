package com.tracker.task.repository;

import com.tracker.task.BaseIntegrationTest;
import com.tracker.task.model.Task;
import com.tracker.task.model.TaskStatus;
import com.tracker.task.model.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskTypeRepository taskTypeRepository;

    private TaskStatus status;
    private TaskType taskType;

    static {
        System.setProperty("DOCKER_HOST", "unix:///var/run/docker.sock");
    }

    @BeforeEach
    void setUp() {
        status = taskStatusRepository.findByCode("PENDING").orElseThrow();
        taskType = taskTypeRepository.findById(1L).orElseThrow();
    }

    @Test
    void shouldSaveAndFindTask() {
        Task task = Task.builder()
                .title("Integration Test")
                .description("Description")
                .taskType(taskType)
                .status(status)
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();

        Task saved = taskRepository.save(task);
        Task found = taskRepository.findById(saved.getId()).orElseThrow();

        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Integration Test");
    }

    @Test
    void shouldFindTasksByStatus() {
        Task task = Task.builder()
                .title("Status Test")
                .description("For status search")
                .taskType(taskType)
                .status(status)
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();
        taskRepository.save(task);
        Page<Task> tasks = taskRepository.findByStatusId(status.getId(), Pageable.unpaged());
        assertThat(tasks).isNotNull();
        assertThat(tasks.getContent()).isNotEmpty();
    }

    @Test
    void shouldFindTasksByDateRange() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(10);

        // Создаём задачу в этом диапазоне
        Task task = Task.builder()
                .title("Date Range Test")
                .description("For date range search")
                .taskType(taskType)
                .status(status)
                .datePlanFinal(LocalDateTime.now().plusDays(5))
                .build();
        taskRepository.save(task);

        Page<Task> tasks = taskRepository.findByDatePlanFinalBetween(from, to, Pageable.unpaged());
        assertThat(tasks).isNotNull();
    }

    @Test
    void shouldFindTasksByUserId() {
        Long userId = 1L;
        Page<Task> tasks = taskRepository.findTasksByUserId(userId, Pageable.unpaged());
        assertThat(tasks).isNotNull();
    }

    @Test
    void shouldFindTasksByStatusAndDateRange() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(10);

        Task task = Task.builder()
                .title("Status + Date Test")
                .description("Test")
                .taskType(taskType)
                .status(status)
                .datePlanFinal(LocalDateTime.now().plusDays(5))
                .build();
        taskRepository.save(task);

        Page<Task> tasks = taskRepository.findByStatusIdAndDatePlanFinalBetween(
                status.getId(), from, to, Pageable.unpaged());

        assertThat(tasks).isNotNull();
        assertThat(tasks.getContent()).isNotEmpty();
    }

    @Test
    void shouldFindTasksByUserIdAndStatus() {
        Long userId = 1L;
        Task task = Task.builder()
                .title("User + Status Test")
                .description("Test")
                .taskType(taskType)
                .status(status)
                .datePlanFinal(LocalDateTime.now().plusDays(5))
                .build();
        taskRepository.save(task);

        Page<Task> tasks = taskRepository.findTasksByUserIdAndStatusId(
                userId, status.getId(), Pageable.unpaged());

        assertThat(tasks).isNotNull();
    }

    @Test
    void shouldFindTasksByUserIdAndDateRange() {
        Long userId = 1L;
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(10);

        Task task = Task.builder()
                .title("User + Date Test")
                .description("Test")
                .taskType(taskType)
                .status(status)
                .datePlanFinal(LocalDateTime.now().plusDays(5))
                .build();
        taskRepository.save(task);

        Page<Task> tasks = taskRepository.findTasksByUserIdAndDatePlanFinalBetween(
                userId, from, to, Pageable.unpaged());

        assertThat(tasks).isNotNull();
    }

    @Test
    void shouldCheckTaskExists() {
        Task task = Task.builder()
                .title("Exists Test")
                .description("Test")
                .taskType(taskType)
                .status(status)
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();

        Task saved = taskRepository.save(task);

        boolean exists = taskRepository.existsById(saved.getId());
        assertThat(exists).isTrue();

        boolean notExists = taskRepository.existsById(999L);
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldDeleteTask() {
        Task task = Task.builder()
                .title("Delete Test")
                .description("Test")
                .taskType(taskType)
                .status(status)
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();

        Task saved = taskRepository.save(task);
        Long id = saved.getId();

        taskRepository.deleteById(id);

        boolean exists = taskRepository.existsById(id);
        assertThat(exists).isFalse();
    }
}