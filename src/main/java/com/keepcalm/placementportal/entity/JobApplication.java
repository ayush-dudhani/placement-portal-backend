package com.keepcalm.placementportal.entity;

import jakarta.persistence.*;
import com.keepcalm.placementportal.enums.ApplicationStatus;
import lombok.*;

import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "job_applications", uniqueConstraints=@UniqueConstraint(name="uk_application_role_student",columnNames={"drive_role_id","student_id"}), indexes={@Index(name="idx_application_status",columnList="status"),@Index(name="idx_application_institution",columnList="institution_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drive_role_id", nullable = false)
    private DriveRole driveRole;

    private Long driveId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false,length=40)
    private ApplicationStatus status;

    private Instant appliedAt;

    @Column(name="eligibility_passed",nullable=false)
    private boolean eligibilityPassed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="eligibility_snapshot",nullable=false,columnDefinition="jsonb")
    private Map<String,Object> eligibilitySnapshot;

    @Column(name="idempotency_key",length=100)
    private String idempotencyKey;

    @Column(name="withdrawal_reason",length=500)
    private String withdrawalReason;
}
