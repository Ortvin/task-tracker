package com.tracker.task.controller;

import com.tracker.task.BaseIntegrationTest;
import com.tracker.task.dto.request.TaskCreateRequest;
import com.tracker.task.dto.response.TaskResponse;
import com.tracker.task.model.*;
import com.tracker.task.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskTypeRepository taskTypeRepository;

    @Autowired
    private UserContactRepository userContactRepository;

    @Autowired
    private ContactTypeRepository contactTypeRepository;

    private TaskStatus status;
    private TaskType taskType;

    @BeforeEach
    void setUp() {
        status = taskStatusRepository.findByCode("PENDING").orElseThrow();
        taskType = taskTypeRepository.findById(1L).orElseThrow();

        if (personRepository.findByUserId(1L).isEmpty()) {
            Person person = new Person();
            person.setUserId(1L);
            person.setName("Test");
            person.setLastName("User");
            person.setIsJuridical(false);
            personRepository.save(person);
        }

        if (userContactRepository.findByUserIdAndIsPrimaryTrue(1L).isEmpty()) {
            ContactType emailType = contactTypeRepository.findByCode("EMAIL")
                    .orElseThrow(() -> new RuntimeException("EMAIL contact type not found"));
            UserContact contact = UserContact.builder()
                    .userId(1L)
                    .contactType(emailType)
                    .value("test@example.com")
                    .isPrimary(true)
                    .build();
            userContactRepository.save(contact);
        }
    }

    @Autowired
    private PersonRepository personRepository;

    private HttpEntity<TaskCreateRequest> createRequest(TaskCreateRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.set("X-Role", "USER");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    private HttpEntity<Void> authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.set("X-Role", "USER");
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Void> adminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.set("X-Role", "ADMIN");
        return new HttpEntity<>(headers);
    }

    @Test
    void createTask_ShouldReturn201() {
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("Controller Test")
                .description("Description")
                .taskTypeId(taskType.getId())
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();

        ResponseEntity<TaskResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/tasks",
                createRequest(request),
                TaskResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Controller Test");
    }

    @Test
    void getTasks_ShouldReturn200() {
        taskRepository.deleteAll();

        // Убеждаемся, что Person и email созданы
        if (personRepository.findByUserId(1L).isEmpty()) {
            Person person = new Person();
            person.setUserId(1L);
            person.setName("Test");
            person.setLastName("User");
            person.setIsJuridical(false);
            personRepository.save(person);
        }

        if (userContactRepository.findByUserIdAndIsPrimaryTrue(1L).isEmpty()) {
            ContactType emailType = contactTypeRepository.findByCode("EMAIL")
                    .orElseThrow(() -> new RuntimeException("EMAIL contact type not found"));
            UserContact contact = UserContact.builder()
                    .userId(1L)
                    .contactType(emailType)
                    .value("test@example.com")
                    .isPrimary(true)
                    .build();
            userContactRepository.save(contact);
        }

        // Создаём задачу через REST (должна добавиться)
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("Get Tasks Test")
                .description("Description")
                .taskTypeId(taskType.getId())
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();

        ResponseEntity<TaskResponse> createResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/tasks",
                createRequest(request),
                TaskResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Проверяем список
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/tasks",
                HttpMethod.GET,
                authHeaders(),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Get Tasks Test");
    }

    @Test
    void getTaskById_ShouldReturn200() {
        // Создаём задачу
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("GetById Controller Test")
                .description("Description")
                .taskTypeId(taskType.getId())
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();

        ResponseEntity<TaskResponse> createdResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/tasks",
                createRequest(request),
                TaskResponse.class
        );

        Long taskId = createdResponse.getBody().getId();

        ResponseEntity<TaskResponse> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/tasks/" + taskId,
                HttpMethod.GET,
                authHeaders(),
                TaskResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("GetById Controller Test");
    }

    @Test
    void deleteTask_ShouldReturn204_WhenAdmin() {
        Task task = Task.builder()
                .title("Delete Test")
                .description("Description")
                .taskType(taskType)
                .status(status)
                .datePlanFinal(LocalDateTime.now().plusDays(7))
                .build();
        Task saved = taskRepository.save(task);

        ResponseEntity<Void> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/tasks/" + saved.getId(),
                HttpMethod.DELETE,
                adminHeaders(),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        boolean exists = taskRepository.existsById(saved.getId());
        assertThat(exists).isFalse();
    }
}