package com.tracker.task.repository;

import com.tracker.task.model.ParticipantRole;
import com.tracker.task.model.TaskParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskParticipantRepository extends JpaRepository<TaskParticipant, Long> {

    Optional<TaskParticipant> findByTaskIdAndRoleId(Long taskId, Long roleId);

    @Query("SELECT COUNT(tp) > 0 FROM TaskParticipant tp " +
            "WHERE tp.task.id = :taskId AND tp.participant.id IN (" +
            "   SELECT p.id FROM Person p WHERE p.userId = :userId" +
            ")")
    boolean isUserParticipant(@Param("taskId") Long taskId, @Param("userId") Long userId);

    @Query("SELECT tp.role FROM TaskParticipant tp " +
            "WHERE tp.task.id = :taskId AND tp.participant.id IN (" +
            "   SELECT p.id FROM Person p WHERE p.userId = :userId" +
            ")")
    Optional<ParticipantRole> findRoleByTaskIdAndUserId(@Param("taskId") Long taskId, @Param("userId") Long userId);
}