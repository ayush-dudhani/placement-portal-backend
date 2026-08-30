package com.keepcalm.placementportal.entity;
import com.keepcalm.placementportal.enums.RoundResultStatus;import jakarta.persistence.*;import lombok.*;import java.math.BigDecimal;
@Entity @Table(name="round_results")@Getter @Setter @NoArgsConstructor
public class RoundResult extends AuditedEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@OneToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="participant_id",nullable=false,unique=true)private RoundParticipant participant;
 @Enumerated(EnumType.STRING)@Column(nullable=false,length=30)private RoundResultStatus status=RoundResultStatus.PENDING;@Column(precision=8,scale=2)private BigDecimal score;@Column(length=1000)private String notes;@Column(nullable=false)private boolean published;
}
