package com.keepcalm.placementportal.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="companies", uniqueConstraints=@UniqueConstraint(name="uk_company_institution_name", columnNames={"institution_id","name"}))
@Getter @Setter @NoArgsConstructor
public class Company extends AuditedEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="institution_id",nullable=false) private Institution institution;
 @Column(nullable=false,length=255) private String name;
 @Column(length=100) private String industry;
 @Column(columnDefinition="text") private String description;
 @Column(name="website_url",length=500) private String websiteUrl;
 @Column(name="logo_url",length=500) private String logoUrl;
 @Column(name="is_active",nullable=false) private boolean active=true;
}
