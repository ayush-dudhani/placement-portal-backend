package com.keepcalm.placementportal.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="drive_roles", indexes=@Index(name="idx_role_drive",columnList="drive_id")) @Getter @Setter @NoArgsConstructor
public class DriveRole extends AuditedEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="drive_id",nullable=false) private CampusDrive drive;
 @Column(nullable=false,length=255) private String title;
 @Column(columnDefinition="text") private String description;
 @Column(nullable=false) private int positions;
 @Column(name="employment_type",length=50) private String employmentType;
 @Column(length=255) private String location;
 @Column(name="package_amount",precision=14,scale=2) private BigDecimal packageAmount;
 @Column(nullable=false,length=3) private String currency="INR";
 @Column(name="application_deadline") private Instant applicationDeadline;
 @Column(name="is_active",nullable=false) private boolean active=true;
}
