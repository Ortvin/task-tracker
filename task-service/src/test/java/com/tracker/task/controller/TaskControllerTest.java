package com.tracker.task.controller;

import com.tracker.task.dto.response.TaskResponse;
import com.tracker.task.mapper.TaskMapper;
import com.tracker.task.model.Task;
import com.tracker.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskMapper taskMapper;

    @Test
    void getTasks_ShouldReturnPageOfTasks() throws Exception {
        Task taskEntity = new Task();
        taskEntity.setId(1L);
        taskEntity.setTitle("Test Task");

        TaskResponse response = new TaskResponse();
        response.setId(1L);
        response.setTitle("Test Task");

        Page<Task> taskPage = new PageImpl<>(List.of(taskEntity));

        when(taskService.getFilteredTasks(
                any(), any(), any(), any(Pageable.class), any(), any()
        )).thenReturn(taskPage);

        when(taskMapper.toResponse(any(Task.class))).thenReturn(response);

        mockMvc.perform(get("/api/tasks")
                        .header("X-User-Id", "1")
                        .header("X-Role", "USER")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Task"));
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenExists() throws Exception {
        Long taskId = 1L;

        Task taskEntity = new Task();
        taskEntity.setId(taskId);
        taskEntity.setTitle("Task by ID");

        TaskResponse response = new TaskResponse();
        response.setId(taskId);
        response.setTitle("Task by ID");

        when(taskService.getTaskById(eq(taskId), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .header("X-User-Id", "1")
                        .header("X-Role", "USER")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Task by ID"));
    }

    @Test
    void createTask_ShouldReturnCreated() throws Exception {
        String requestJson = """
            {
                "title": "New Task",
                "description": "Description",
                "taskTypeId": 1,
                "datePlanFinal": "2026-09-01T18:00:00"
            }
            """;

        TaskResponse response = new TaskResponse();
        response.setId(1L);
        response.setTitle("New Task");

        when(taskService.createTask(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/tasks")
                        .header("X-User-Id", "1")
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    void deleteTask_ShouldReturnNoContent_WhenAdmin() throws Exception {
        Long taskId = 1L;

        doNothing().when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/tasks/{id}", taskId)
                        .header("X-User-Id", "1")
                        .header("X-Role", "ADMIN")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_ShouldReturnForbidden_WhenUser() throws Exception {
        Long taskId = 1L;

        mockMvc.perform(delete("/api/tasks/{id}", taskId)
                        .header("X-User-Id", "2")
                        .header("X-Role", "USER")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTask_ShouldReturnUpdatedTask() throws Exception {
        Long taskId = 1L;
        String requestJson = """
            {
                "title": "Updated Title",
                "description": "Updated Description",
                "taskTypeId": 1,
                "datePlanFinal": "2026-09-01T18:00:00"
            }
            """;

        TaskResponse response = new TaskResponse();
        response.setId(taskId);
        response.setTitle("Updated Title");

        when(taskService.updateTask(eq(taskId), any(), eq(1L), eq("USER"))).thenReturn(response);

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .header("X-User-Id", "1")
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void patchTask_ShouldReturnPatchedTask() throws Exception {
        Long taskId = 1L;
        String requestJson = """
            {
                "statusId": 3
            }
            """;

        TaskResponse response = new TaskResponse();
        response.setId(taskId);
        response.setTitle("Old Title");

        when(taskService.patchTask(eq(taskId), any(), eq(1L), eq("USER"))).thenReturn(response);

        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                        .header("X-User-Id", "1")
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId));
    }

}