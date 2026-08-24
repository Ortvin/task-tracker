package com.tracker.task.repository;

import com.tracker.task.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {
    Optional<TaskStatus> findByCode(String code);
}