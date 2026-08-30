package com.keepcalm.placementportal.service.drive;
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
import com.keepcalm.placementportal.api.PagedResponse;
import com.keepcalm.placementportal.entity.auth.*;
import com.keepcalm.placementportal.entity.profile.*;
import com.keepcalm.placementportal.entity.student.*;
import com.keepcalm.placementportal.entity.company.*;
import com.keepcalm.placementportal.entity.drive.*;
import com.keepcalm.placementportal.entity.application.*;
import com.keepcalm.placementportal.entity.selection.*;
import com.keepcalm.placementportal.entity.offer.*;
import com.keepcalm.placementportal.entity.communication.*;
import com.keepcalm.placementportal.entity.audit.*;
import com.keepcalm.placementportal.enums.DriveStatus;
import com.keepcalm.placementportal.exception.DomainException;
import com.keepcalm.placementportal.models.drive.DriveDtos.*;
import com.keepcalm.placementportal.repository.auth.*;
import com.keepcalm.placementportal.repository.profile.*;
import com.keepcalm.placementportal.repository.student.*;
import com.keepcalm.placementportal.repository.company.*;
import com.keepcalm.placementportal.repository.drive.*;
import com.keepcalm.placementportal.repository.application.*;
import com.keepcalm.placementportal.repository.selection.*;
import com.keepcalm.placementportal.repository.offer.*;
import com.keepcalm.placementportal.repository.communication.*;
import com.keepcalm.placementportal.repository.audit.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor
public class DriveDiscoveryService {
 private static final Set<DriveStatus> VISIBLE=EnumSet.complementOf(EnumSet.of(DriveStatus.DRAFT,DriveStatus.CANCELLED));
 private final CampusDriveRepository drives;private final DriveRoleRepository roles;private final EligibilityRuleRepository rules;
 private final StudentRepository students;private final AcademicRecordRepository academics;private final CurrentUserService current;private final EligibilityEngine engine;
 @Cacheable(cacheNames="drives",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true) public PagedResponse<DriveDto> list(String query,Long companyId,Pageable pageable){return PagedResponse.from(drives.discover(current.principal().institutionId(),VISIBLE,companyId,blank(query),pageable).map(d->dto(d,false)));}
 @Cacheable(cacheNames="drives",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true) public DriveDto get(Long id){return dto(visible(id),true);}
 @Cacheable(cacheNames="drives",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true) public List<RoleDto> roles(Long driveId){visible(driveId);return roles.findByDriveIdAndActiveTrueOrderByTitleAsc(driveId).stream().map(this::roleDto).toList();}
 @Cacheable(cacheNames="drives",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true) public RoleDto role(Long driveId,Long roleId){visible(driveId);return roleDto(roles.findByIdAndDriveIdAndActiveTrue(roleId,driveId).orElseThrow(()->DomainException.notFound("Drive role not found")));}
 @Transactional(readOnly=true) public EligibilityDto eligibility(Long driveId,Long roleId){CampusDrive drive=visible(driveId);DriveRole role=roles.findByIdAndDriveIdAndActiveTrue(roleId,driveId).orElseThrow(()->DomainException.notFound("Drive role not found"));Student s=students.findByUserId(current.getCurrentUser().getId()).orElseThrow(()->DomainException.notFound("Student profile not found"));AcademicRecord a=academics.findByStudentId(s.getId()).orElseThrow(()->DomainException.unprocessable("ACADEMICS_INCOMPLETE","Academic record is required"));EligibilityEngine.Evaluation result=engine.evaluate(s,a,rules.applicable(drive.getId(),role.getId()));return new EligibilityDto(result.eligible(),result.explanations(),result.snapshot());}
 public RuleDto ruleDto(EligibilityRule r){return new RuleDto(r.getId(),r.getDriveRole()==null?null:r.getDriveRole().getId(),r.getRuleType(),r.getConfiguration(),r.isMandatory(),r.isActive());}
 public DriveDto dto(CampusDrive d,boolean includeRoles){List<RoleDto> items=includeRoles?roles.findByDriveIdAndActiveTrueOrderByTitleAsc(d.getId()).stream().map(this::roleDto).toList():List.of();return new DriveDto(d.getId(),d.getCompany()==null?null:d.getCompany().getId(),d.getCompany()==null?null:d.getCompany().getName(),d.getTitle(),d.getDescription(),d.getStatus(),d.getStartsAt(),d.getEndsAt(),d.getApplicationsOpenAt(),d.getApplicationDeadline(),d.getOwner()==null?null:d.getOwner().getId(),d.getVersion(),items);}
 public RoleDto roleDto(DriveRole r){return new RoleDto(r.getId(),r.getTitle(),r.getDescription(),r.getPositions(),r.getEmploymentType(),r.getLocation(),r.getPackageAmount(),r.getCurrency(),r.getApplicationDeadline(),r.isActive(),r.getVersion());}
 private CampusDrive visible(Long id){return drives.findByIdAndInstitutionId(id,current.principal().institutionId()).filter(d->VISIBLE.contains(d.getStatus())&&!d.isArchived()).orElseThrow(()->DomainException.notFound("Drive not found"));}
 private String blank(String value){return value==null||value.isBlank()?null:value.trim();}
}
