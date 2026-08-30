package com.keepcalm.placementportal.entity;
import jakarta.persistence.*;import lombok.*;import java.math.BigDecimal;import java.time.Instant;
@Entity @Table(name="placement_outcomes")@Getter @Setter @NoArgsConstructor
public class PlacementOutcome extends AuditedEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@OneToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="offer_id",nullable=false,unique=true)private Offer offer;
 @ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="student_id",nullable=false)private Student student;@ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="company_id",nullable=false)private Company company;
 @Column(name="package_amount",nullable=false,precision=14,scale=2)private BigDecimal packageAmount;@Column(nullable=false,length=3)private String currency;@Column(name="placed_at",nullable=false)private Instant placedAt;
}
