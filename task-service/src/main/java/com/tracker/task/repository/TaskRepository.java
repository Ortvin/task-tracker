package com.tracker.task.repository;

import com.tracker.task.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.datePlanFinal < :now AND t.dateFactFinal IS NULL")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE DATE(t.dateCreated) = :date")
    List<Task> findByDateCreated(@Param("date") LocalDate date);

    @Query("SELECT t FROM Task t WHERE DATE(t.datePlanFinal) = :date AND t.status.code != :statusCode")
    List<Task> findByDatePlanFinalAndStatusNot(@Param("date") LocalDate date, @Param("statusCode") String statusCode);

    Page<Task> findByStatusId(Long statusId, Pageable pageable);

    Page<Task> findByDatePlanFinalBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<Task> findByStatusIdAndDatePlanFinalBetween(
            Long statusId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    @Query("SELECT t FROM Task t JOIN t.participants tp JOIN tp.participant p " +
            "WHERE p.id IN (SELECT per.id FROM Person per WHERE per.userId = :userId)")
    Page<Task> findTasksByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE EXISTS (" +
            "SELECT tp FROM TaskParticipant tp " +
            "WHERE tp.task = t AND tp.participant.id IN (" +
            "   SELECT p.id FROM Person p WHERE p.userId = :userId" +
            ") AND t.status.id = :statusId)")
    Page<Task> findTasksByUserIdAndStatusId(@Param("userId") Long userId, @Param("statusId") Long statusId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE EXISTS (" +
            "SELECT tp FROM TaskParticipant tp " +
            "WHERE tp.task = t AND tp.participant.id IN (" +
            "   SELECT p.id FROM Person p WHERE p.userId = :userId" +
            ") AND t.datePlanFinal BETWEEN :from AND :to)")
    Page<Task> findTasksByUserIdAndDatePlanFinalBetween(@Param("userId") Long userId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE EXISTS (" +
            "SELECT tp FROM TaskParticipant tp " +
            "WHERE tp.task = t AND tp.participant.id IN (" +
            "   SELECT p.id FROM Person p WHERE p.userId = :userId" +
            ") AND t.status.id = :statusId AND t.datePlanFinal BETWEEN :from AND :to)")
    Page<Task> findTasksByUserIdAndStatusIdAndDatePlanFinalBetween(
            @Param("userId") Long userId,
            @Param("statusId") Long statusId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}