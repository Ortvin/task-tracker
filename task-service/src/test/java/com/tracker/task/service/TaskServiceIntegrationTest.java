package com.tracker.task.service;

import com.tracker.task.BaseIntegrationTest;
import com.tracker.task.dto.request.TaskCreateRequest;
import com.tracker.task.dto.response.TaskResponse;
import com.tracker.task.model.*;
import com.tracker.task.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskTypeRepository taskTypeRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ParticipantRoleRepository participantRoleRepository;

    @Autowired
    private UserContactRepository userContactRepository;

    @Autowired
    private ContactTypeRepository contactTypeRepository;

    @Autowired
    private TaskParticipantRepository taskParticipantRepository;

    private TaskStatus status;
    private TaskType taskType;
    private Person author;
    private ParticipantRole authorRole;

    @BeforeEach
    void setUp() {
        status = taskStatusRepository.findByCode("PENDING").orElseThrow();
        taskType = taskTypeRepository.findById(1L).orElseThrow();
        authorRole = participantRoleRepository.findById(1L).orElseThrow();

        author = personRepository.findByUserId(1L).orElseGet(() -> {
            Person p = new Person();
            p.setUserId(1L);
            p.setName("Author");
            p.setLastName("Test");
            p.setIsJuridical(false);
            return personRepository.save(p);
        });

        if (userContactRepository.findByUserIdAndIsPrimaryTrue(1L).isEmpty()) {
            ContactType emailType = contactTypeRepository.findByCode("EMAIL").orElseThrow();
            UserContact contact = UserContact.builder()
                    .userId(1L)
                    .contactType(emailType)
                    .value("test@example.com")
                    .isPrimary(true)
                    .build();
            userContactRepository.save(contact);
        }
    }

    @Test
    void createTask_ShouldSaveAndReturnTask() {
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("Integration Test Task")
                .description("Description")
                .taskTypeId(taskType.getId())
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();

        TaskResponse response = taskService.createTask(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Integration Test Task");
        assertThat(response.getParticipants()).isNotEmpty();
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenExists() {
        // Создаём задачу
        Task task = Task.builder()
                .title("GetById Test")
                .description("Description")
                .taskType(taskType)
                .status(status)
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();
        Task saved = taskRepository.save(task);

        // Добавляем участника вручную
        TaskParticipant participant = TaskParticipant.builder()
                .task(saved)
                .participant(author)
                .role(authorRole)
                .build();
        taskParticipantRepository.save(participant);

        TaskResponse response = taskService.getTaskById(saved.getId(), 1L, "USER");
        assertThat(response).isNotNull();
    }

    @Test
    void getFilteredTasks_ShouldReturnPage() {
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("Filter Test")
                .description("Description")
                .taskTypeId(taskType.getId())
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();

        taskService.createTask(request, 1L);

        Page<Task> tasks = taskService.getFilteredTasks(
                null, null, null,
                PageRequest.of(0, 10),
                1L, "USER"
        );

        assertThat(tasks).isNotNull();
        assertThat(tasks.getTotalElements()).isGreaterThan(0);
    }

    @Test
    void updateTask_ShouldUpdate_WhenUserIsAdmin() {
        Task task = Task.builder()
                .title("Update Test")
                .description("Description")
                .taskType(taskType)
                .status(status)
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();
        Task saved = taskRepository.save(task);

        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .taskTypeId(taskType.getId())
                .datePlanFinal(LocalDateTime.now().plusDays(14))
                .build();

        TaskResponse response = taskService.updateTask(saved.getId(), request, 1L, "ADMIN");

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void deleteTask_ShouldDelete_WhenAdmin() {
        Task task = Task.builder()
                .title("Delete Test")
                .description("Description")
                .taskType(taskType)
                .status(status)
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();
        Task saved = taskRepository.save(task);

        taskService.deleteTask(saved.getId());

        boolean exists = taskRepository.existsById(saved.getId());
        assertThat(exists).isFalse();
    }
}