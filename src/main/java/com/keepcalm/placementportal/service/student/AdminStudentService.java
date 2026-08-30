package com.keepcalm.placementportal.service.student;
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
import com.keepcalm.placementportal.enums.*;
import com.keepcalm.placementportal.exception.DomainException;
import com.keepcalm.placementportal.models.admin.AdminStudentDtos.*;
import com.keepcalm.placementportal.models.application.ApplicationDtos.ApplicationDto;
import com.keepcalm.placementportal.models.student.StudentDocumentDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
@Service @RequiredArgsConstructor
public class AdminStudentService {
 private final StudentRepository students;private final AcademicRecordRepository academics;private final StudentDocumentRepository documents;private final JobApplicationRepository applications;private final ApplicationService applicationMapper;
 private final UserRepository users;private final PasswordEncoder passwords;private final CurrentUserService current;private final AuditService audit;private final IdempotencyRecordRepository idempotency;private final ObjectMapper json;
 @Transactional(readOnly=true) public PagedResponse<StudentDto> list(String query,String branch,Integer year,PlacementStatus status,Pageable p){return PagedResponse.from(students.adminSearch(current.principal().institutionId(),blank(query),blank(branch),year,status,p).map(this::dto));}
 @Transactional(readOnly=true) public StudentDto get(Long id){return dto(student(id));}
 @Transactional public StudentDto placement(Long id,PlacementUpdate r){if(r.status()==PlacementStatus.PLACED||r.status()==PlacementStatus.SELECTED)throw DomainException.unprocessable("PLACEMENT_STATUS_DERIVED","Placed/selected status is derived from a verified offer");Student s=student(id);s.setPlacementStatus(r.status());audit.record("STUDENT_PLACEMENT_STATUS_CHANGED","Student",id,Map.of("status",r.status().name()));return dto(s);}
 @Transactional(readOnly=true) public List<ApplicationDto> applications(Long id){Student s=student(id);return applications.findByStudentIdOrderByAppliedAtDesc(s.getId()).stream().map(applicationMapper::dto).toList();}
 @Transactional(readOnly=true) public List<StudentDocumentDto> documents(Long id){Student s=student(id);return documents.findByStudentIdOrderByCreatedAtDesc(s.getId()).stream().map(this::documentDto).toList();}
 @Transactional public StudentDocumentDto verify(Long studentId,Long documentId,VerificationUpdate r){Student s=student(studentId);StudentDocument d=documents.findByIdAndStudentIdAndStudentInstitutionId(documentId,s.getId(),current.principal().institutionId()).orElseThrow(()->DomainException.notFound("Document not found"));d.setVerificationStatus(r.status());d.setVerificationNote(r.note());d.setVerifiedBy(current.getCurrentUser());audit.record("DOCUMENT_VERIFICATION_CHANGED","StudentDocument",documentId,Map.of("studentId",studentId,"status",r.status().name()));return documentDto(d);}
 @Transactional(readOnly=true) public byte[] export(String query,String branch,Integer year,PlacementStatus status){List<Student> list=students.adminSearch(current.principal().institutionId(),blank(query),blank(branch),year,status,Pageable.unpaged()).getContent();StringBuilder out=new StringBuilder("studentId,username,email,rollNumber,firstName,lastName,branch,graduationYear,cgpa,placementStatus\n");for(Student s:list)out.append(s.getId()).append(',').append(csv(s.getUser().getUsername())).append(',').append(csv(s.getUser().getEmail())).append(',').append(csv(s.getRollNumber())).append(',').append(csv(s.getFirstName())).append(',').append(csv(s.getLastName())).append(',').append(csv(s.getBranch())).append(',').append(Objects.toString(s.getYearOfPassing(),"")).append(',').append(Objects.toString(s.getCgpa(),"")).append(',').append(s.getPlacementStatus()).append('\n');return out.toString().getBytes(StandardCharsets.UTF_8);}
 @Transactional public Map<String,Object> importStudents(MultipartFile file,String key){byte[] bytes=bytes(file);Map<String,Object> replay=replay("STUDENT_IMPORT",key,bytes);if(replay!=null)return replay;String[] lines=new String(bytes,StandardCharsets.UTF_8).split("\\R");if(lines.length<2)throw new DomainException(HttpStatus.BAD_REQUEST,"EMPTY_IMPORT","CSV must contain data");int created=0;Institution institution=current.getCurrentUser().getInstitution();for(int i=1;i<lines.length;i++){if(lines[i].isBlank())continue;String[] c=lines[i].split(",",8);if(c.length<8)throw new DomainException(HttpStatus.BAD_REQUEST,"INVALID_CSV","Expected username,email,password,rollNumber,firstName,lastName,branch,graduationYear at line "+(i+1));String username=c[0].trim(),email=c[1].trim().toLowerCase();if(users.existsByInstitutionIdAndUsernameIgnoreCase(institution.getId(),username)||users.existsByInstitutionIdAndEmailIgnoreCase(institution.getId(),email))throw DomainException.conflict("ACCOUNT_EXISTS","Duplicate account at line "+(i+1));User u=new User();u.setInstitution(institution);u.setUsername(username);u.setEmail(email);u.setPasswordHash(passwords.encode(c[2].trim()));u.setRole(Role.STUDENT);u.setIsActive(true);u.setEmailVerified(true);users.save(u);Student s=new Student();s.setInstitution(institution);s.setUser(u);s.setRollNumber(c[3].trim());s.setFirstName(c[4].trim());s.setLastName(c[5].trim());s.setBranch(c[6].trim());s.setYearOfPassing(Integer.valueOf(c[7].trim()));s.setPlacementStatus(PlacementStatus.NOT_PLACED);students.save(s);AcademicRecord a=new AcademicRecord();a.setStudent(s);a.setBranch(s.getBranch());a.setGraduationYear(s.getYearOfPassing());academics.save(a);created++;}Map<String,Object> response=Map.of("created",created);save("STUDENT_IMPORT",key,bytes,response);audit.record("STUDENTS_IMPORTED","Student",null,response);return response;}
 private Student student(Long id){return students.findByIdAndInstitutionId(id,current.principal().institutionId()).orElseThrow(()->DomainException.notFound("Student not found"));}
 private StudentDto dto(Student s){return new StudentDto(s.getId(),s.getUser().getUsername(),s.getUser().getEmail(),s.getFirstName(),s.getLastName(),s.getRollNumber(),s.getBranch(),s.getYearOfPassing(),s.getCgpa(),s.getActiveBacklogs()==null?0:s.getActiveBacklogs(),s.getPlacementStatus(),s.getProfileCompletion()==null?0:s.getProfileCompletion(),s.getVersion());}
 private StudentDocumentDto documentDto(StudentDocument d){return new StudentDocumentDto(d.getId(),d.getDocumentType(),d.getOriginalFilename(),d.getContentType(),d.getSizeBytes(),d.isPrimaryDocument(),d.getVerificationStatus(),d.getVerificationNote(),d.getCreatedAt(),d.getVersion());}
 private Map<String,Object> replay(String op,String key,byte[] bytes){requireKey(key);return idempotency.findByInstitutionIdAndActorIdAndOperationAndIdempotencyKey(current.principal().institutionId(),current.principal().userId(),op,key).map(r->{if(!r.getRequestHash().equals(hash(bytes)))throw DomainException.conflict("IDEMPOTENCY_KEY_REUSED","Idempotency key has different content");try{return json.readValue(r.getResponseJson(),Map.class);}catch(Exception e){throw new IllegalStateException(e);}}).orElse(null);}
 private void save(String op,String key,byte[] bytes,Map<String,Object> response){try{User actor=current.getCurrentUser();IdempotencyRecord r=new IdempotencyRecord();r.setInstitution(actor.getInstitution());r.setActor(actor);r.setOperation(op);r.setIdempotencyKey(key);r.setRequestHash(hash(bytes));r.setResponseJson(json.writeValueAsString(response));idempotency.save(r);}catch(Exception e){throw new IllegalStateException(e);}}
 private void requireKey(String key){if(key==null||key.isBlank())throw new DomainException(HttpStatus.BAD_REQUEST,"IDEMPOTENCY_KEY_REQUIRED","Idempotency-Key header is required");}
 private byte[] bytes(MultipartFile f){try{return f.getBytes();}catch(Exception e){throw new DomainException(HttpStatus.BAD_REQUEST,"INVALID_FILE","Import cannot be read");}}
 private String hash(byte[] b){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));}catch(Exception e){throw new IllegalStateException(e);}}
 private String csv(String v){return "\""+Objects.toString(v,"").replace("\"","\"\"")+"\"";}private String blank(String s){return s==null||s.isBlank()?null:s.trim();}
}
