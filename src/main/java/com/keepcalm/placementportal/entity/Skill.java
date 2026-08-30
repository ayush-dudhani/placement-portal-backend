package com.keepcalm.placementportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skills", uniqueConstraints = @UniqueConstraint(name = "uk_skill_institution_name", columnNames = {"institution_id", "normalized_name"}))
@Getter @Setter @NoArgsConstructor
public class Skill extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;
    @Column(nullable = false, length = 100) private String name;
    @Column(name = "normalized_name", nullable = false, length = 100) private String normalizedName;
}
