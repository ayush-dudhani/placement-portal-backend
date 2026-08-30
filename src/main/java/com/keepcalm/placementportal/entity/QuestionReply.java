package com.keepcalm.placementportal.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="question_replies") @Getter @Setter @NoArgsConstructor
public class QuestionReply extends AuditedEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="question_id",nullable=false) private CompanyQuestion question;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="author_id",nullable=false) private User author;
 @Column(nullable=false,columnDefinition="text") private String body;
}
