package com.keepcalm.placementportal.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="company_faqs") @Getter @Setter @NoArgsConstructor
public class CompanyFaq extends AuditedEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="company_id",nullable=false) private Company company;
 @Column(nullable=false,length=500) private String question;
 @Column(nullable=false,columnDefinition="text") private String answer;
 @Column(name="display_order",nullable=false) private int displayOrder;
}
