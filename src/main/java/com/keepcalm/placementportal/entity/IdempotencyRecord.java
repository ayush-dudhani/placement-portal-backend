package com.keepcalm.placementportal.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name="idempotency_records",uniqueConstraints=@UniqueConstraint(name="uk_idempotency_scope",columnNames={"institution_id","actor_id","operation","idempotency_key"})) @Getter @Setter @NoArgsConstructor
public class IdempotencyRecord {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="institution_id",nullable=false) private Institution institution;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="actor_id",nullable=false) private User actor;
 @Column(nullable=false,length=100) private String operation;
 @Column(name="idempotency_key",nullable=false,length=100) private String idempotencyKey;
 @Column(name="request_hash",nullable=false,length=64) private String requestHash;
 @Column(name="response_json",columnDefinition="text") private String responseJson;
 @Column(name="created_at",nullable=false) private Instant createdAt=Instant.now();
}
