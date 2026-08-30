package com.keepcalm.placementportal.service.application;
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
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.keepcalm.placementportal.enums.ApplicationStatus;
import com.keepcalm.placementportal.exception.DomainException;
import com.keepcalm.placementportal.models.application.ApplicationDtos.*;
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
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
@Service @RequiredArgsConstructor
public class AdminApplicationService {
 private final JobApplicationRepository applications;private final ApplicationStatusHistoryRepository history;private final ApplicationService mapper;private final ApplicationStatusPolicy policy;
 private final AcademicRecordRepository academics;private final EligibilityRuleRepository rules;private final EligibilityEngine engine;private final CurrentUserService current;private final AuditService audit;private final IdempotencyRecordRepository idempotency;private final ObjectMapper json;
 @Transactional(readOnly=true) public PagedResponse<ApplicationDto> list(ApplicationStatus status,Long driveId,Long roleId,String query,Pageable p){return PagedResponse.from(applications.adminSearch(current.principal().institutionId(),status,driveId,roleId,blank(query),p).map(mapper::dto));}
 @Transactional(readOnly=true) public ApplicationDto get(Long id){return mapper.dto(application(id));}
 @Transactional public ApplicationDto status(Long id,StatusUpdate r){JobApplication a=application(id);change(a,r.status(),r.reason());audit.record("APPLICATION_STATUS_CHANGED","JobApplication",id,Map.of("status",r.status().name()));return mapper.dto(a);}
 @Transactional public Map<String,Object> bulkStatus(BulkStatusUpdate r){for(Long id:r.applicationIds())change(application(id),r.status(),r.reason());audit.record("APPLICATION_BULK_STATUS_CHANGED","JobApplication",null,Map.of("count",r.applicationIds().size(),"status",r.status().name()));return Map.of("updated",r.applicationIds().size());}
 @Transactional(readOnly=true) public List<Map<String,Object>> bulkEligibility(BulkIds request){List<Map<String,Object>> result=new ArrayList<>();for(Long id:request.applicationIds()){JobApplication a=application(id);AcademicRecord academic=academics.findByStudentId(a.getStudent().getId()).orElse(null);boolean eligible=academic!=null&&engine.evaluate(a.getStudent(),academic,rules.applicable(a.getDriveRole().getDrive().getId(),a.getDriveRole().getId())).eligible();result.add(Map.of("applicationId",id,"currentStatus",a.getStatus().name(),"currentlyEligible",eligible,"originalEligibility",a.isEligibilityPassed()));}return result;}
 @Transactional public Map<String,Object> importResults(MultipartFile file,String key){byte[] bytes=bytes(file);Map<String,Object> replay=replay("APPLICATION_RESULTS_IMPORT",key,bytes);if(replay!=null)return replay;String text=new String(bytes,StandardCharsets.UTF_8);String[] lines=text.split("\\R");if(lines.length<2)throw new DomainException(HttpStatus.BAD_REQUEST,"EMPTY_IMPORT","CSV must contain a header and data");int updated=0;for(int i=1;i<lines.length;i++){if(lines[i].isBlank())continue;String[] c=lines[i].split(",",3);if(c.length<2)throw new DomainException(HttpStatus.BAD_REQUEST,"INVALID_CSV","Expected applicationId,status,reason at line "+(i+1));Long id=Long.valueOf(c[0].trim());ApplicationStatus status=ApplicationStatus.valueOf(c[1].trim().toUpperCase());change(application(id),status,c.length==3?c[2].trim():"Imported result");updated++;}Map<String,Object> response=Map.of("updated",updated);saveIdempotency("APPLICATION_RESULTS_IMPORT",key,bytes,response);audit.record("APPLICATION_RESULTS_IMPORTED","JobApplication",null,response);return response;}
 @Transactional(readOnly=true) public byte[] export(Long driveId,ApplicationStatus status,String query){List<JobApplication> list=applications.adminSearch(current.principal().institutionId(),status,driveId,null,blank(query),Pageable.unpaged()).getContent();StringBuilder out=new StringBuilder("applicationId,studentId,rollNumber,studentName,driveId,roleId,status,appliedAt\n");for(JobApplication a:list)out.append(a.getId()).append(',').append(a.getStudent().getId()).append(',').append(csv(a.getStudent().getRollNumber())).append(',').append(csv((a.getStudent().getFirstName()+" "+Objects.toString(a.getStudent().getLastName(),"")).trim())).append(',').append(a.getDriveRole().getDrive().getId()).append(',').append(a.getDriveRole().getId()).append(',').append(a.getStatus()).append(',').append(a.getAppliedAt()).append('\n');return out.toString().getBytes(StandardCharsets.UTF_8);}
 private void change(JobApplication a,ApplicationStatus to,String reason){policy.require(a.getStatus(),to);ApplicationStatus from=a.getStatus();a.setStatus(to);ApplicationStatusHistory h=new ApplicationStatusHistory();h.setApplication(a);h.setFromStatus(from);h.setToStatus(to);h.setChangedBy(current.getCurrentUser());h.setReason(reason);history.save(h);}
 private JobApplication application(Long id){return applications.findByIdAndInstitutionId(id,current.principal().institutionId()).orElseThrow(()->DomainException.notFound("Application not found"));}
 private Map<String,Object> replay(String operation,String key,byte[] bytes){requireKey(key);return idempotency.findByInstitutionIdAndActorIdAndOperationAndIdempotencyKey(current.principal().institutionId(),current.principal().userId(),operation,key).map(record->{if(!record.getRequestHash().equals(hash(bytes)))throw DomainException.conflict("IDEMPOTENCY_KEY_REUSED","Idempotency key was used with different content");try{return json.readValue(record.getResponseJson(),Map.class);}catch(Exception e){throw new IllegalStateException(e);}}).orElse(null);}
 private void saveIdempotency(String operation,String key,byte[] bytes,Map<String,Object> response){try{User actor=current.getCurrentUser();IdempotencyRecord r=new IdempotencyRecord();r.setInstitution(actor.getInstitution());r.setActor(actor);r.setOperation(operation);r.setIdempotencyKey(key);r.setRequestHash(hash(bytes));r.setResponseJson(json.writeValueAsString(response));idempotency.save(r);}catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException(e);}}
 private void requireKey(String key){if(key==null||key.isBlank())throw new DomainException(HttpStatus.BAD_REQUEST,"IDEMPOTENCY_KEY_REQUIRED","Idempotency-Key header is required");if(key.length()>100)throw new DomainException(HttpStatus.BAD_REQUEST,"INVALID_IDEMPOTENCY_KEY","Idempotency-Key must be at most 100 characters");}
 private byte[] bytes(MultipartFile file){try{return file.getBytes();}catch(Exception e){throw new DomainException(HttpStatus.BAD_REQUEST,"INVALID_FILE","Import file cannot be read");}}
 private String hash(byte[] bytes){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}catch(Exception e){throw new IllegalStateException(e);}}
 private String csv(String value){String v=Objects.toString(value,"");return "\""+v.replace("\"","\"\"")+"\"";}
 private String blank(String s){return s==null||s.isBlank()?null:s.trim();}
}
