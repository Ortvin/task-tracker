package com.tracker.task.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "organisations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "participant_id")
public class Organisation extends Participant {

    @Column(name = "short_name", nullable = false, length = 100)
    private String shortName;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(length = 50)
    private String ogrn;

    @Column(name = "user_id", unique = true)
    private Long userId;
}