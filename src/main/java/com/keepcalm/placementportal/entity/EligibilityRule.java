package com.keepcalm.placementportal.entity;
import com.keepcalm.placementportal.enums.EligibilityRuleType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;
@Entity @Table(name="eligibility_rules", indexes=@Index(name="idx_rule_drive",columnList="drive_id")) @Getter @Setter @NoArgsConstructor
public class EligibilityRule extends AuditedEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="drive_id",nullable=false) private CampusDrive drive;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="drive_role_id") private DriveRole driveRole;
 @Enumerated(EnumType.STRING) @Column(name="rule_type",nullable=false,length=50) private EligibilityRuleType ruleType;
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="configuration_json",nullable=false,columnDefinition="jsonb") private Map<String,Object> configuration;
 @Column(nullable=false) private boolean mandatory=true;
 @Column(name="is_active",nullable=false) private boolean active=true;
}
