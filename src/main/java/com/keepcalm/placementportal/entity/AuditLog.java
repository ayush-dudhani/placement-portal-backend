package com.keepcalm.placementportal.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
@Entity @Table(name="audit_logs",indexes={@Index(name="idx_audit_institution_created",columnList="institution_id,created_at"),@Index(name="idx_audit_resource",columnList="resource_type,resource_id")}) @Getter @Setter @NoArgsConstructor
public class AuditLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="institution_id",nullable=false,updatable=false) private Institution institution;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="actor_id",nullable=false,updatable=false) private User actor;
 @Column(nullable=false,length=100,updatable=false) private String action;
 @Column(name="resource_type",nullable=false,length=100,updatable=false) private String resourceType;
 @Column(name="resource_id",length=100,updatable=false) private String resourceId;
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="details_json",nullable=false,columnDefinition="jsonb",updatable=false) private Map<String,Object> details=Map.of();
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt=Instant.now();
}
