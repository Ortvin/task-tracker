package com.tracker.task.repository;

import com.tracker.task.model.ParticipantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRoleRepository extends JpaRepository<ParticipantRole, Long> {
    Optional<ParticipantRole> findByName(String name);
}