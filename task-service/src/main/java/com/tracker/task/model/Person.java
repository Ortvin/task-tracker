package com.tracker.task.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "persons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "participant_id")
public class Person extends Participant {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String middleName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(name = "user_id", unique = true)
    private Long userId;
}