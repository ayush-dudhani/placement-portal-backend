package com.keepcalm.placementportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_skill_assignments", uniqueConstraints = @UniqueConstraint(name = "uk_student_skill", columnNames = {"student_id", "skill_id"}))
@Getter @Setter @NoArgsConstructor
public class StudentSkill extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "student_id", nullable = false) private Student student;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "skill_id", nullable = false) private Skill skill;
}
