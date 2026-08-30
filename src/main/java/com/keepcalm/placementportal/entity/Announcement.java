package com.keepcalm.placementportal.entity;
import com.keepcalm.placementportal.enums.Role;import jakarta.persistence.*;import lombok.*;import java.time.Instant;
@Entity @Table(name="announcements",indexes=@Index(name="idx_announcement_institution_published",columnList="institution_id,published_at"))@Getter @Setter @NoArgsConstructor
public class Announcement extends AuditedEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="institution_id",nullable=false)private Institution institution;
 @ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="created_by",nullable=false)private User createdBy;@Column(nullable=false,length=255)private String title;@Column(nullable=false,columnDefinition="text")private String content;
 @Enumerated(EnumType.STRING)@Column(name="audience_role",length=30)private Role audienceRole;@Column(name="published_at")private Instant publishedAt;@Column(name="expires_at")private Instant expiresAt;@Column(nullable=false)private boolean archived;
}
