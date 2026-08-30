package com.keepcalm.placementportal.models.drive;
import com.keepcalm.placementportal.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
public final class DriveDtos {
 private DriveDtos(){}
 public record DriveDto(Long id,Long companyId,String companyName,String title,String description,DriveStatus status,
   Instant startsAt,Instant endsAt,Instant applicationsOpenAt,Instant applicationDeadline,Long ownerId,long version,List<RoleDto> roles){}
 public record RoleDto(Long id,String title,String description,int positions,String employmentType,String location,BigDecimal packageAmount,String currency,Instant applicationDeadline,boolean active,long version){}
 public record RuleDto(Long id,Long roleId,EligibilityRuleType ruleType,Map<String,Object> configuration,boolean mandatory,boolean active){}
 public record DriveUpsert(Long companyId,@NotBlank @Size(max=255)String title,@Size(max=10000)String description,
   Instant startsAt,Instant endsAt,Instant applicationsOpenAt,Instant applicationDeadline,Long ownerId){}
 public record RoleUpsert(@NotBlank @Size(max=255)String title,@Size(max=10000)String description,@Min(1)int positions,
   @Size(max=50)String employmentType,@Size(max=255)String location,@DecimalMin("0.0")BigDecimal packageAmount,
   @Pattern(regexp="^[A-Z]{3}$")String currency,Instant applicationDeadline){}
 public record RuleUpsert(Long roleId,@NotNull EligibilityRuleType ruleType,@NotNull Map<String,Object> configuration,boolean mandatory,boolean active){}
 public record RulesUpdate(@NotEmpty List<@Valid RuleUpsert> rules){}
 public record EligibilityDto(boolean eligible,List<String> explanations,Map<String,Object> snapshot){}
}
