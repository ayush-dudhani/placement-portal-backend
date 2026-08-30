package com.keepcalm.placementportal.entity;
import com.keepcalm.placementportal.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name="application_status_history", indexes=@Index(name="idx_history_application",columnList="application_id")) @Getter @Setter @NoArgsConstructor
public class ApplicationStatusHistory {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="application_id",nullable=false,updatable=false) private JobApplication application;
 @Enumerated(EnumType.STRING) @Column(name="from_status",length=40,updatable=false) private ApplicationStatus fromStatus;
 @Enumerated(EnumType.STRING) @Column(name="to_status",nullable=false,length=40,updatable=false) private ApplicationStatus toStatus;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="changed_by",updatable=false) private User changedBy;
 @Column(length=500,updatable=false) private String reason;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt=Instant.now();
}
