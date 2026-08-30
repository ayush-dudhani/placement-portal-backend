package com.keepcalm.placementportal.models.drive;
import com.keepcalm.placementportal.repository.audit.*;
import com.keepcalm.placementportal.repository.communication.*;
import com.keepcalm.placementportal.repository.offer.*;
import com.keepcalm.placementportal.repository.selection.*;
import com.keepcalm.placementportal.repository.application.*;
import com.keepcalm.placementportal.repository.drive.*;
import com.keepcalm.placementportal.repository.company.*;
import com.keepcalm.placementportal.repository.profile.*;
import com.keepcalm.placementportal.repository.student.*;
import com.keepcalm.placementportal.repository.auth.*;
import com.keepcalm.placementportal.service.storage.*;
import com.keepcalm.placementportal.service.audit.*;
import com.keepcalm.placementportal.service.event.*;
import com.keepcalm.placementportal.service.analytics.*;
import com.keepcalm.placementportal.service.communication.*;
import com.keepcalm.placementportal.service.offer.*;
import com.keepcalm.placementportal.service.selection.*;
import com.keepcalm.placementportal.service.application.*;
import com.keepcalm.placementportal.service.drive.*;
import com.keepcalm.placementportal.service.company.*;
import com.keepcalm.placementportal.service.student.*;
import com.keepcalm.placementportal.service.profile.*;
import com.keepcalm.placementportal.service.auth.*;
import com.keepcalm.placementportal.entity.audit.*;
import com.keepcalm.placementportal.entity.communication.*;
import com.keepcalm.placementportal.entity.offer.*;
import com.keepcalm.placementportal.entity.selection.*;
import com.keepcalm.placementportal.entity.application.*;
import com.keepcalm.placementportal.entity.drive.*;
import com.keepcalm.placementportal.entity.company.*;
import com.keepcalm.placementportal.entity.profile.*;
import com.keepcalm.placementportal.entity.student.*;
import com.keepcalm.placementportal.entity.auth.*;
import com.keepcalm.placementportal.controller.event.*;
import com.keepcalm.placementportal.controller.analytics.*;
import com.keepcalm.placementportal.controller.communication.*;
import com.keepcalm.placementportal.controller.offer.*;
import com.keepcalm.placementportal.controller.selection.*;
import com.keepcalm.placementportal.controller.application.*;
import com.keepcalm.placementportal.controller.drive.*;
import com.keepcalm.placementportal.controller.company.*;
import com.keepcalm.placementportal.controller.student.*;
import com.keepcalm.placementportal.controller.profile.*;
import com.keepcalm.placementportal.controller.auth.*;
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
