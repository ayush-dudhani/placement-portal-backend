package com.keepcalm.placementportal.entity;
import com.keepcalm.placementportal.enums.DriveStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name="campus_drives", indexes={@Index(name="idx_drive_status",columnList="status"),@Index(name="idx_drive_deadline",columnList="application_deadline"),@Index(name="idx_drive_institution",columnList="institution_id")})
@Getter @Setter @NoArgsConstructor
public class CampusDrive extends AuditedEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="institution_id",nullable=false) private Institution institution;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id") private Company company;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="owner_id") private User owner;
 @Column(nullable=false,length=255) private String title;
 @Column(columnDefinition="text") private String description;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=40) private DriveStatus status=DriveStatus.DRAFT;
 @Column(name="starts_at") private Instant startsAt;
 @Column(name="ends_at") private Instant endsAt;
 @Column(name="applications_open_at") private Instant applicationsOpenAt;
 @Column(name="application_deadline") private Instant applicationDeadline;
 @Column(nullable=false) private boolean archived;
}
