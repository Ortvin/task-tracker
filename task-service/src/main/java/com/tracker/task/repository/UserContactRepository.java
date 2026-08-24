package com.tracker.task.repository;

import com.tracker.task.model.UserContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserContactRepository extends JpaRepository<UserContact, Long> {
    Optional<UserContact> findByUserIdAndIsPrimaryTrue(Long userId);
}