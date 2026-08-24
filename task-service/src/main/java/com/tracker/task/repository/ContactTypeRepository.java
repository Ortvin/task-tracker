package com.tracker.task.repository;

import com.tracker.task.model.ContactType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContactTypeRepository extends JpaRepository<ContactType, Long> {
    Optional<ContactType> findByCode(String code);
}