package com.keepcalm.placementportal.service;
import com.keepcalm.placementportal.api.PagedResponse;
import com.keepcalm.placementportal.entity.*;
import com.keepcalm.placementportal.enums.*;
import com.keepcalm.placementportal.exception.*;
import com.keepcalm.placementportal.models.application.ApplicationDtos.*;
import com.keepcalm.placementportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
@Service @RequiredArgsConstructor
public class ApplicationService {
 private final JobApplicationRepository applications;private final ApplicationStatusHistoryRepository history;private final CampusDriveRepository drives;
 private final DriveRoleRepository roles;private final EligibilityRuleRepository rules;private final StudentRepository students;private final AcademicRecordRepository academics;
 private final CurrentUserService current;private final EligibilityEngine engine;private final ApplicationStatusPolicy policy;
 @CacheEvict(cacheNames="analytics",allEntries=true) @Transactional(noRollbackFor=EligibilityFailedException.class)
 public ApplicationDto apply(Long driveId,Long roleId,String idempotencyKey){
  Student student=student();String key=validateKey(idempotencyKey);
  if(key!=null){Optional<JobApplication> previous=applications.findByStudentIdAndIdempotencyKey(student.getId(),key);if(previous.isPresent()){if(!previous.get().getDriveRole().getId().equals(roleId))throw DomainException.conflict("IDEMPOTENCY_KEY_REUSED","Idempotency key was used for another application");return dto(previous.get());}}
  CampusDrive drive=drives.findByIdAndInstitutionId(driveId,current.principal().institutionId()).orElseThrow(()->DomainException.notFound("Drive not found"));
  DriveRole role=roles.findByIdAndDriveIdAndActiveTrue(roleId,driveId).orElseThrow(()->DomainException.notFound("Drive role not found"));
  requireOpen(drive,role);
  if(applications.existsByDriveRoleIdAndStudentId(roleId,student.getId()))throw DomainException.conflict("DUPLICATE_APPLICATION","Student has already applied to this drive role");
  AcademicRecord academic=academics.findByStudentId(student.getId()).orElseThrow(()->DomainException.unprocessable("ACADEMICS_INCOMPLETE","Academic record is required before applying"));
  List<EligibilityRule> applicable=rules.applicable(driveId,roleId);if(applicable.isEmpty())throw DomainException.conflict("DRIVE_NOT_CONFIGURED","Drive has no eligibility rules");
  EligibilityEngine.Evaluation evaluation=engine.evaluate(student,academic,applicable);
  JobApplication application=new JobApplication();application.setStudent(student);application.setInstitution(student.getInstitution());application.setDriveRole(role);application.setDriveId(driveId);application.setAppliedAt(Instant.now());application.setEligibilityPassed(evaluation.eligible());application.setEligibilitySnapshot(evaluation.snapshot());application.setIdempotencyKey(key);application.setStatus(evaluation.eligible()?ApplicationStatus.ELIGIBLE:ApplicationStatus.INELIGIBLE);
  applications.saveAndFlush(application);addHistory(application,null,application.getStatus(),student.getUser(),String.join("; ",evaluation.explanations()));
  if(!evaluation.eligible())throw new EligibilityFailedException(evaluation.explanations().isEmpty()?"Student is not eligible":String.join("; ",evaluation.explanations()));
  return dto(application);
 }
 @Transactional(readOnly=true) public PagedResponse<ApplicationDto> list(ApplicationStatus status,Long driveId,Long roleId,Pageable pageable){return PagedResponse.from(applications.studentApplications(student().getId(),status,driveId,roleId,pageable).map(this::dto));}
 @Transactional(readOnly=true) public ApplicationDto get(Long id){return dto(owned(id));}
 @Transactional(readOnly=true) public List<HistoryDto> history(Long id){JobApplication a=owned(id);return history.findByApplicationIdOrderByCreatedAtAsc(a.getId()).stream().map(this::historyDto).toList();}
 @CacheEvict(cacheNames="analytics",allEntries=true) @Transactional public ApplicationDto withdraw(Long id,Withdraw request){JobApplication a=owned(id);policy.requireWithdrawable(a.getStatus());ApplicationStatus from=a.getStatus();a.setStatus(ApplicationStatus.WITHDRAWN);a.setWithdrawalReason(request==null?null:request.reason());addHistory(a,from,a.getStatus(),current.getCurrentUser(),request==null?null:request.reason());return dto(a);}
 private void requireOpen(CampusDrive d,DriveRole r){if(d.getStatus()!=DriveStatus.PUBLISHED&&d.getStatus()!=DriveStatus.APPLICATIONS_OPEN)throw DomainException.conflict("APPLICATIONS_NOT_OPEN","Drive is not accepting applications");Instant now=Instant.now();if(d.getApplicationsOpenAt()!=null&&now.isBefore(d.getApplicationsOpenAt()))throw DomainException.conflict("APPLICATIONS_NOT_OPEN","Application window has not opened");Instant deadline=r.getApplicationDeadline()!=null?r.getApplicationDeadline():d.getApplicationDeadline();if(deadline==null||!now.isBefore(deadline))throw DomainException.conflict("APPLICATION_DEADLINE_PASSED","Application deadline has passed");}
 private void addHistory(JobApplication a,ApplicationStatus from,ApplicationStatus to,User user,String reason){ApplicationStatusHistory h=new ApplicationStatusHistory();h.setApplication(a);h.setFromStatus(from);h.setToStatus(to);h.setChangedBy(user);h.setReason(reason==null?null:reason.substring(0,Math.min(500,reason.length())));history.save(h);}
 private Student student(){return students.findByUserId(current.getCurrentUser().getId()).orElseThrow(()->DomainException.notFound("Student profile not found"));}
 private JobApplication owned(Long id){Student s=student();return applications.findByIdAndStudentId(id,s.getId()).orElseThrow(()->DomainException.notFound("Application not found"));}
 private String validateKey(String key){if(key==null||key.isBlank())return null;String k=key.trim();if(k.length()>100)throw new DomainException(org.springframework.http.HttpStatus.BAD_REQUEST,"INVALID_IDEMPOTENCY_KEY","Idempotency-Key must be at most 100 characters");return k;}
 public ApplicationDto dto(JobApplication a){DriveRole r=a.getDriveRole();return new ApplicationDto(a.getId(),r.getDrive().getId(),r.getDrive().getTitle(),r.getId(),r.getTitle(),a.getStatus(),a.isEligibilityPassed(),a.getEligibilitySnapshot(),a.getAppliedAt(),a.getVersion());}
 private HistoryDto historyDto(ApplicationStatusHistory h){return new HistoryDto(h.getId(),h.getFromStatus(),h.getToStatus(),h.getChangedBy()==null?null:h.getChangedBy().getUsername(),h.getReason(),h.getCreatedAt());}
}
