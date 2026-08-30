package com.keepcalm.placementportal.entity;
import jakarta.persistence.*;import lombok.*;import java.time.Instant;
@Entity @Table(name="selection_rounds",uniqueConstraints=@UniqueConstraint(name="uk_round_drive_order",columnNames={"drive_id","round_order"})) @Getter @Setter @NoArgsConstructor
public class SelectionRound extends AuditedEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="drive_id",nullable=false)private CampusDrive drive;
 @Column(nullable=false,length=255)private String name;@Column(name="round_type",nullable=false,length=50)private String roundType;@Column(name="round_order",nullable=false)private int roundOrder;
 @Column(name="starts_at",nullable=false)private Instant startsAt;@Column(name="ends_at",nullable=false)private Instant endsAt;@Column(length=255)private String location;
 @Column(name="results_published",nullable=false)private boolean resultsPublished;@Column(nullable=false)private boolean archived;
}
