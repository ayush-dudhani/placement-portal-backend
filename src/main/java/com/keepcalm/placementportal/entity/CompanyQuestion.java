package com.keepcalm.placementportal.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="company_questions") @Getter @Setter @NoArgsConstructor
public class CompanyQuestion extends AuditedEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="company_id",nullable=false) private Company company;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="student_id",nullable=false) private Student student;
 @Column(nullable=false,columnDefinition="text") private String question;
 @Column(name="is_anonymous",nullable=false) private boolean anonymous;
 @Column(name="is_closed",nullable=false) private boolean closed;
}
