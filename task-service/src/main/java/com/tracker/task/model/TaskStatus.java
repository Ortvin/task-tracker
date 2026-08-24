package com.tracker.task.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "task_statuses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "sort_order")
    private Integer sortOrder;
}