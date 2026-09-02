package com.tracker.task.controller;

import com.tracker.task.dto.request.TaskCreateRequest;
import com.tracker.task.dto.request.TaskPatchRequest;
import com.tracker.task.dto.response.TaskResponse;
import com.tracker.task.dto.response.TaskStatusHistoryResponse;
import com.tracker.task.mapper.TaskMapper;
import com.tracker.task.model.Task;
import com.tracker.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @RequestBody @Valid TaskCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        if (userId == null) {
            throw new RuntimeException("User ID not found in request");
        }
        TaskResponse response = taskService.createTask(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role") String role
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<Task> tasks = taskService.getFilteredTasks(statusId, fromDate, toDate, pageable, userId, role);
        return ResponseEntity.ok(tasks.map(taskMapper::toResponse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @RequestBody @Valid TaskCreateRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role") String role
    ) {
        return ResponseEntity.ok(taskService.updateTask(id, request, userId, role));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> patchTask(
            @PathVariable Long id,
            @RequestBody TaskPatchRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role") String role
    ) {
        return ResponseEntity.ok(taskService.patchTask(id, request, userId, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role") String role
    ) {
        return ResponseEntity.ok(taskService.getTaskById(id, userId, role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @RequestHeader("X-Role") String role
    ) {
        if (!"ADMIN".equals(role)) {
            throw new AccessDeniedException("Only ADMIN can delete tasks");
        }
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<TaskStatusHistoryResponse>> getTaskHistory(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role") String role
    ) {
        return ResponseEntity.ok(taskService.getTaskStatusHistory(id, userId, role));
    }
}