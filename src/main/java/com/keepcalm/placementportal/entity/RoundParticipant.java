package com.keepcalm.placementportal.entity;
import com.keepcalm.placementportal.enums.AttendanceStatus;import jakarta.persistence.*;import lombok.*;
@Entity @Table(name="round_participants",uniqueConstraints=@UniqueConstraint(name="uk_round_application",columnNames={"round_id","application_id"}))@Getter @Setter @NoArgsConstructor
public class RoundParticipant extends AuditedEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="round_id",nullable=false)private SelectionRound round;
 @ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="application_id",nullable=false)private JobApplication application;
 @Enumerated(EnumType.STRING)@Column(name="attendance_status",nullable=false,length=20)private AttendanceStatus attendanceStatus=AttendanceStatus.REGISTERED;
}
