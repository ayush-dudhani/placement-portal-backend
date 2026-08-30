package com.keepcalm.placementportal.service.offer;
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
import com.keepcalm.placementportal.controller.auth.*;import com.fasterxml.jackson.databind.ObjectMapper;import com.keepcalm.placementportal.api.PagedResponse;import com.keepcalm.placementportal.entity.auth.*;
import com.keepcalm.placementportal.entity.profile.*;
import com.keepcalm.placementportal.entity.student.*;
import com.keepcalm.placementportal.entity.company.*;
import com.keepcalm.placementportal.entity.drive.*;
import com.keepcalm.placementportal.entity.application.*;
import com.keepcalm.placementportal.entity.selection.*;
import com.keepcalm.placementportal.entity.offer.*;
import com.keepcalm.placementportal.entity.communication.*;
import com.keepcalm.placementportal.entity.audit.*;
import com.keepcalm.placementportal.entity.common.*;import com.keepcalm.placementportal.enums.*;import com.keepcalm.placementportal.exception.DomainException;import com.keepcalm.placementportal.models.offer.OfferDtos.*;import com.keepcalm.placementportal.repository.auth.*;
import com.keepcalm.placementportal.repository.profile.*;
import com.keepcalm.placementportal.repository.student.*;
import com.keepcalm.placementportal.repository.company.*;
import com.keepcalm.placementportal.repository.drive.*;
import com.keepcalm.placementportal.repository.application.*;
import com.keepcalm.placementportal.repository.selection.*;
import com.keepcalm.placementportal.repository.offer.*;
import com.keepcalm.placementportal.repository.communication.*;
import com.keepcalm.placementportal.repository.audit.*;
import lombok.RequiredArgsConstructor;import org.springframework.cache.annotation.CacheEvict;import org.springframework.data.domain.*;import org.springframework.http.HttpStatus;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import org.springframework.web.multipart.MultipartFile;import java.nio.charset.StandardCharsets;import java.security.MessageDigest;import java.time.Instant;import java.util.*;
@Service @RequiredArgsConstructor public class OfferService{
 private final OfferRepository offers;private final PlacementOutcomeRepository outcomes;private final JobApplicationRepository applications;private final ApplicationStatusHistoryRepository history;private final StudentRepository students;private final CurrentUserService current;private final ApplicationStatusPolicy policy;private final AuditService audit;private final NotificationService notifications;private final IdempotencyRecordRepository idempotency;private final ObjectMapper json;
 @Transactional(readOnly=true)public PagedResponse<OfferDto> mine(Pageable p){return PagedResponse.from(offers.findByStudentId(student().getId(),p).map(this::dto));}@Transactional(readOnly=true)public OfferDto mine(Long id){return dto(owned(id));}
 @CacheEvict(cacheNames={"analytics","companies"},allEntries=true) @Transactional public OfferDto accept(Long id){Offer o=owned(id);requireRespondable(o);if(!o.isVerified())throw DomainException.unprocessable("OFFER_NOT_VERIFIED","Offer must be verified before it can be accepted");o.setStatus(OfferStatus.ACCEPTED);changeApplication(o.getApplication(),ApplicationStatus.OFFER_ACCEPTED,"Student accepted offer");materializeOutcome(o);return dto(o);}
 @CacheEvict(cacheNames="analytics",allEntries=true) @Transactional public OfferDto decline(Long id){Offer o=owned(id);requireRespondable(o);o.setStatus(OfferStatus.DECLINED);changeApplication(o.getApplication(),ApplicationStatus.OFFER_DECLINED,"Student declined offer");return dto(o);}
 @Transactional(readOnly=true)public PagedResponse<OfferDto> adminList(OfferStatus status,Long companyId,Pageable p){return PagedResponse.from(offers.adminSearch(current.principal().institutionId(),status,companyId,p).map(this::dto));}
 @Transactional public OfferDto create(Upsert r){JobApplication a=application(r.applicationId());if(a.getStatus()!=ApplicationStatus.SELECTED)throw DomainException.unprocessable("APPLICATION_NOT_SELECTED","Offer can only be created for a selected application");Offer o=new Offer();o.setInstitution(a.getInstitution());o.setApplication(a);o.setStudent(a.getStudent());o.setCompany(a.getDriveRole().getDrive().getCompany());apply(o,r);offers.save(o);releaseIfRequested(o,r.release());audit.record("OFFER_CREATED","Offer",o.getId(),Map.of("applicationId",a.getId(),"verified",o.isVerified()));return dto(o);}
 @Transactional public OfferDto update(Long id,Upsert r){Offer o=adminOffer(id);if(!o.getApplication().getId().equals(r.applicationId()))throw DomainException.conflict("OFFER_APPLICATION_IMMUTABLE","Offer application cannot be changed");boolean becameVerified=!o.isVerified()&&r.verified();apply(o,r);releaseIfRequested(o,r.release());if(becameVerified&&o.getStatus()==OfferStatus.ACCEPTED)materializeOutcome(o);audit.record("OFFER_UPDATED","Offer",id,Map.of("verified",o.isVerified(),"status",o.getStatus().name()));return dto(o);}
 @Transactional public Map<String,Object> importOffers(MultipartFile file,String key){byte[] bytes=bytes(file);Map<String,Object> replay=replay(key,bytes);if(replay!=null)return replay;String[] lines=new String(bytes,StandardCharsets.UTF_8).split("\\R");if(lines.length<2)throw new DomainException(HttpStatus.BAD_REQUEST,"EMPTY_IMPORT","CSV must contain data");int created=0;for(int i=1;i<lines.length;i++){if(lines[i].isBlank())continue;String[] c=lines[i].split(",",5);if(c.length<5)throw new DomainException(HttpStatus.BAD_REQUEST,"INVALID_CSV","Expected applicationId,amount,currency,verified,release");create(new Upsert(Long.valueOf(c[0].trim()),new java.math.BigDecimal(c[1].trim()),c[2].trim().toUpperCase(),null,Boolean.parseBoolean(c[3].trim()),null,Boolean.parseBoolean(c[4].trim())));created++;}Map<String,Object> response=Map.of("created",created);saveIdempotency(key,bytes,response);audit.record("OFFERS_IMPORTED","Offer",null,response);return response;}
 private void apply(Offer o,Upsert r){o.setPackageAmount(r.packageAmount());o.setCurrency(r.currency());o.setLetterObjectKey(r.letterObjectKey());o.setVerified(r.verified());o.setResponseDeadline(r.responseDeadline());o.setRoleTitle(o.getApplication().getDriveRole().getTitle());}
 private void releaseIfRequested(Offer o,boolean release){if(release&&o.getStatus()==OfferStatus.DRAFT){o.setStatus(OfferStatus.RELEASED);o.setOfferedAt(Instant.now());notifications.notify(o.getStudent().getUser(),"Offer released",o.getCompany().getName()+" has released an offer","OFFER","/student/offers/"+o.getId());}}
 private void requireRespondable(Offer o){if(o.getStatus()!=OfferStatus.RELEASED)throw DomainException.conflict("OFFER_NOT_RESPONDABLE","Offer is not open for response");if(o.getResponseDeadline()!=null&&!Instant.now().isBefore(o.getResponseDeadline()))throw DomainException.conflict("OFFER_RESPONSE_DEADLINE_PASSED","Offer response deadline has passed");}
 private void materializeOutcome(Offer o){if(!o.isVerified()||o.getStatus()!=OfferStatus.ACCEPTED||outcomes.findByOfferId(o.getId()).isPresent())return;PlacementOutcome x=new PlacementOutcome();x.setOffer(o);x.setStudent(o.getStudent());x.setCompany(o.getCompany());x.setPackageAmount(o.getPackageAmount());x.setCurrency(o.getCurrency());x.setPlacedAt(Instant.now());outcomes.save(x);o.getStudent().setPlacementStatus(PlacementStatus.PLACED);audit.record("PLACEMENT_OUTCOME_CREATED","PlacementOutcome",x.getId(),Map.of("offerId",o.getId()));}
 private void changeApplication(JobApplication a,ApplicationStatus to,String reason){policy.require(a.getStatus(),to);ApplicationStatus from=a.getStatus();a.setStatus(to);ApplicationStatusHistory h=new ApplicationStatusHistory();h.setApplication(a);h.setFromStatus(from);h.setToStatus(to);h.setChangedBy(current.getCurrentUser());h.setReason(reason);history.save(h);}
 private Student student(){return students.findByUserId(current.principal().userId()).orElseThrow(()->DomainException.notFound("Student profile not found"));}private Offer owned(Long id){return offers.findByIdAndStudentId(id,student().getId()).orElseThrow(()->DomainException.notFound("Offer not found"));}private Offer adminOffer(Long id){return offers.findByIdAndInstitutionId(id,current.principal().institutionId()).orElseThrow(()->DomainException.notFound("Offer not found"));}private JobApplication application(Long id){return applications.findByIdAndInstitutionId(id,current.principal().institutionId()).orElseThrow(()->DomainException.notFound("Application not found"));}
 private OfferDto dto(Offer o){return new OfferDto(o.getId(),o.getApplication().getId(),o.getStudent().getId(),o.getCompany().getId(),o.getCompany().getName(),o.getRoleTitle(),o.getPackageAmount(),o.getCurrency(),o.getStatus(),o.isVerified(),o.getResponseDeadline(),o.getOfferedAt(),o.getVersion());}
 private Map<String,Object> replay(String key,byte[] bytes){requireKey(key);return idempotency.findByInstitutionIdAndActorIdAndOperationAndIdempotencyKey(current.principal().institutionId(),current.principal().userId(),"OFFER_IMPORT",key).map(r->{if(!r.getRequestHash().equals(hash(bytes)))throw DomainException.conflict("IDEMPOTENCY_KEY_REUSED","Idempotency key has different content");try{return json.readValue(r.getResponseJson(),Map.class);}catch(Exception e){throw new IllegalStateException(e);}}).orElse(null);}private void saveIdempotency(String key,byte[] bytes,Map<String,Object> response){try{User actor=current.getCurrentUser();IdempotencyRecord r=new IdempotencyRecord();r.setInstitution(actor.getInstitution());r.setActor(actor);r.setOperation("OFFER_IMPORT");r.setIdempotencyKey(key);r.setRequestHash(hash(bytes));r.setResponseJson(json.writeValueAsString(response));idempotency.save(r);}catch(Exception e){throw new IllegalStateException(e);}}private void requireKey(String k){if(k==null||k.isBlank())throw new DomainException(HttpStatus.BAD_REQUEST,"IDEMPOTENCY_KEY_REQUIRED","Idempotency-Key header is required");}private byte[] bytes(MultipartFile f){try{return f.getBytes();}catch(Exception e){throw new DomainException(HttpStatus.BAD_REQUEST,"INVALID_FILE","Import cannot be read");}}private String hash(byte[] b){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));}catch(Exception e){throw new IllegalStateException(e);}}
}
