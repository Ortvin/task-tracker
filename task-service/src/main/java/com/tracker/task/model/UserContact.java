package com.tracker.task.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "user_contacts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "contact_type_id", nullable = false)
    private ContactType contactType;

    @Column(nullable = false, length = 255)
    private String value;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;
}