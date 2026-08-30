package com.keepcalm.placementportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "academic_records")
@Getter @Setter @NoArgsConstructor
public class AcademicRecord extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;
    @Column(precision = 4, scale = 2) private BigDecimal cgpa;
    @Column(name = "tenth_percentage", precision = 5, scale = 2) private BigDecimal tenthPercentage;
    @Column(name = "twelfth_percentage", precision = 5, scale = 2) private BigDecimal twelfthPercentage;
    @Column(name = "diploma_percentage", precision = 5, scale = 2) private BigDecimal diplomaPercentage;
    @Column(name = "active_backlogs", nullable = false) private int activeBacklogs;
    @Column(nullable = false, length = 100) private String branch;
    @Column(name = "graduation_year", nullable = false) private int graduationYear;
}
