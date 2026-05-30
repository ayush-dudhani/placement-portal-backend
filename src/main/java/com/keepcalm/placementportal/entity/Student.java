package com.keepcalm.placementportal.entity;

import com.keepcalm.placementportal.enums.PlacementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String collegeName;

    private String firstName;

    private String lastName;

    private String mobileNo;

    @Column(unique = true)
    private String rollNumber;

    private String branch;

    private Integer yearOfPassing;

    private BigDecimal cgpa;

    private Integer activeBacklogs;

    private String resumeUrl;

    private String linkedinUrl;

    private String githubUrl;

    @Enumerated(EnumType.STRING)
    private PlacementStatus placementStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}