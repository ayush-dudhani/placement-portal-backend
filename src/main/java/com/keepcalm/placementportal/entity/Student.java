package com.keepcalm.placementportal.entity;

import com.keepcalm.placementportal.enums.PlacementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
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

    private BigDecimal tenthPercentage;

    private BigDecimal twelfthPercentage;

    private BigDecimal diplomaPercentage;

    private Integer activeBacklogs;

    private String resumeUrl;

    private String linkedinUrl;

    private String githubUrl;

    @Enumerated(EnumType.STRING)
    private PlacementStatus placementStatus;

    @ElementCollection
    @CollectionTable(
            name = "student_skills",
            joinColumns = @JoinColumn(name = "student_id")
    )
    @Column(name = "skill")
    private List<String> skills;

    private Integer profileCompletion;

    private LocalDate dateOfBirth;

    private String gender;

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