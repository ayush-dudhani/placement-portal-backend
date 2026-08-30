package com.keepcalm.placementportal.entity;
import jakarta.persistence.*;import lombok.*;import java.time.Instant;
@Entity @Table(name="notifications",indexes=@Index(name="idx_notification_user_read",columnList="user_id,read_at"))@Getter @Setter @NoArgsConstructor
public class Notification extends AuditedEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="user_id",nullable=false)private User user;
 @Column(nullable=false,length=255)private String title;@Column(nullable=false,columnDefinition="text")private String message;@Column(nullable=false,length=50)private String type;@Column(name="link_url",length=500)private String linkUrl;@Column(name="read_at")private Instant readAt;
}
