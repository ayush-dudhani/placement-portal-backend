package com.keepcalm.placementportal.entity;
import com.keepcalm.placementportal.enums.OfferStatus;import jakarta.persistence.*;import lombok.*;import java.math.BigDecimal;import java.time.Instant;
@Entity @Table(name="offers",indexes={@Index(name="idx_offer_student",columnList="student_id"),@Index(name="idx_offer_status",columnList="status")})@Getter @Setter @NoArgsConstructor
public class Offer extends AuditedEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="institution_id",nullable=false)private Institution institution;
 @OneToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="application_id",nullable=false,unique=true)private JobApplication application;@ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="student_id",nullable=false)private Student student;
 @ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="company_id",nullable=false)private Company company;@Column(name="role_title",nullable=false,length=255)private String roleTitle;
 @Column(name="package_amount",nullable=false,precision=14,scale=2)private BigDecimal packageAmount;@Column(nullable=false,length=3)private String currency;@Column(name="letter_object_key",length=500)private String letterObjectKey;
 @Enumerated(EnumType.STRING)@Column(nullable=false,length=30)private OfferStatus status=OfferStatus.DRAFT;@Column(nullable=false)private boolean verified;@Column(name="response_deadline")private Instant responseDeadline;@Column(name="offered_at")private Instant offeredAt;
}
