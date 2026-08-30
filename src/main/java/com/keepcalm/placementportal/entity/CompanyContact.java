package com.keepcalm.placementportal.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="company_contacts") @Getter @Setter @NoArgsConstructor
public class CompanyContact extends AuditedEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="company_id",nullable=false) private Company company;
 @Column(nullable=false,length=150) private String name;
 @Column(nullable=false,length=255) private String email;
 @Column(length=30) private String phone;
 @Column(length=100) private String designation;
 @Column(name="is_primary",nullable=false) private boolean primaryContact;
}
