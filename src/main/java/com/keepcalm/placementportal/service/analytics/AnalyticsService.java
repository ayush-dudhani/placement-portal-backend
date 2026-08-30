package com.keepcalm.placementportal.service.analytics;
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
import com.keepcalm.placementportal.controller.auth.*;import com.keepcalm.placementportal.api.PagedResponse;import com.keepcalm.placementportal.entity.auth.*;
import com.keepcalm.placementportal.entity.profile.*;
import com.keepcalm.placementportal.entity.student.*;
import com.keepcalm.placementportal.entity.company.*;
import com.keepcalm.placementportal.entity.drive.*;
import com.keepcalm.placementportal.entity.application.*;
import com.keepcalm.placementportal.entity.selection.*;
import com.keepcalm.placementportal.entity.offer.*;
import com.keepcalm.placementportal.entity.communication.*;
import com.keepcalm.placementportal.entity.audit.*;
import com.keepcalm.placementportal.entity.common.*;import com.keepcalm.placementportal.enums.*;import com.keepcalm.placementportal.repository.auth.*;
import com.keepcalm.placementportal.repository.profile.*;
import com.keepcalm.placementportal.repository.student.*;
import com.keepcalm.placementportal.repository.company.*;
import com.keepcalm.placementportal.repository.drive.*;
import com.keepcalm.placementportal.repository.application.*;
import com.keepcalm.placementportal.repository.selection.*;
import com.keepcalm.placementportal.repository.offer.*;
import com.keepcalm.placementportal.repository.communication.*;
import com.keepcalm.placementportal.repository.audit.*;
import lombok.RequiredArgsConstructor;import org.springframework.cache.annotation.Cacheable;import org.springframework.data.domain.*;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.math.*;import java.nio.charset.StandardCharsets;import java.util.*;import java.util.stream.Collectors;
@Service @RequiredArgsConstructor public class AnalyticsService{
 private final StudentRepository students;private final JobApplicationRepository applications;private final CampusDriveRepository drives;private final CompanyRepository companies;private final PlacementOutcomeRepository outcomes;private final OfferRepository offers;private final StudentDocumentRepository documents;private final AuditLogRepository audits;private final CurrentUserService current;
 @Cacheable(cacheNames="analytics",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true)public Map<String,Object> overview(){Long iid=current.principal().institutionId();long studentCount=students.countByInstitutionId(iid),placed=outcomes.countByStudentInstitutionId(iid);long appCount=applications.adminSearch(iid,null,null,null,null,Pageable.unpaged()).getTotalElements();long openDrives=drives.adminSearch(iid,DriveStatus.APPLICATIONS_OPEN,null,null,Pageable.unpaged()).getTotalElements();return Map.of("students",studentCount,"placedStudents",placed,"placementRate",studentCount==0?0:BigDecimal.valueOf(placed*100.0/studentCount).setScale(2,RoundingMode.HALF_UP),"applications",appCount,"openDrives",openDrives,"companies",companies.search(iid,null,Pageable.unpaged()).getTotalElements());}
 @Cacheable(cacheNames="analytics",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true)public List<Map<String,Object>> applicationAnalytics(){Long iid=current.principal().institutionId();Map<ApplicationStatus,Long> grouped=applications.adminSearch(iid,null,null,null,null,Pageable.unpaged()).stream().collect(Collectors.groupingBy(JobApplication::getStatus,Collectors.counting()));return Arrays.stream(ApplicationStatus.values()).map(s->Map.<String,Object>of("status",s.name(),"count",grouped.getOrDefault(s,0L))).toList();}
 @Cacheable(cacheNames="analytics",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true)public List<Map<String,Object>> placementsByBranch(){return outcomes.findByStudentInstitutionId(current.principal().institutionId()).stream().collect(Collectors.groupingBy(o->Objects.toString(o.getStudent().getBranch(),"UNSPECIFIED"),Collectors.counting())).entrySet().stream().map(e->Map.<String,Object>of("branch",e.getKey(),"placed",e.getValue())).toList();}
 @Cacheable(cacheNames="analytics",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true)public Map<String,Object> packages(){List<BigDecimal> values=outcomes.findByStudentInstitutionId(current.principal().institutionId()).stream().map(PlacementOutcome::getPackageAmount).toList();if(values.isEmpty())return Map.of("count",0,"currency","INR","average",0,"minimum",0,"maximum",0);BigDecimal sum=values.stream().reduce(BigDecimal.ZERO,BigDecimal::add);return Map.of("count",values.size(),"currency","MIXED","average",sum.divide(BigDecimal.valueOf(values.size()),2,RoundingMode.HALF_UP),"minimum",values.stream().min(BigDecimal::compareTo).orElseThrow(),"maximum",values.stream().max(BigDecimal::compareTo).orElseThrow());}
 @Cacheable(cacheNames="analytics",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true)public List<Map<String,Object>> companyPerformance(){return outcomes.findByStudentInstitutionId(current.principal().institutionId()).stream().collect(Collectors.groupingBy(o->o.getCompany().getName())).entrySet().stream().map(e->Map.<String,Object>of("company",e.getKey(),"placements",e.getValue().size(),"averagePackage",e.getValue().stream().map(PlacementOutcome::getPackageAmount).reduce(BigDecimal.ZERO,BigDecimal::add).divide(BigDecimal.valueOf(e.getValue().size()),2,RoundingMode.HALF_UP))).toList();}
 public byte[] export(){Map<String,Object> o=overview();String csv="metric,value\n"+o.entrySet().stream().map(e->e.getKey()+","+e.getValue()).collect(Collectors.joining("\n"))+"\n";return csv.getBytes(StandardCharsets.UTF_8);}
 @Cacheable(cacheNames="analytics",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true)public Map<String,Object> dashboard(){Map<String,Object> data=new LinkedHashMap<>(overview());data.put("pendingDocuments",documents.countByStudentInstitutionIdAndVerificationStatus(current.principal().institutionId(),DocumentVerificationStatus.PENDING));data.put("draftOffers",offers.countByInstitutionIdAndStatus(current.principal().institutionId(),OfferStatus.DRAFT));return data;}
 @Cacheable(cacheNames="analytics",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true)public List<Map<String,Object>> actionItems(){Map<String,Object> d=dashboard();List<Map<String,Object>> result=new ArrayList<>();result.add(Map.of("type","DOCUMENT_VERIFICATION","count",d.get("pendingDocuments")));result.add(Map.of("type","DRAFT_OFFERS","count",d.get("draftOffers")));result.add(Map.of("type","OPEN_DRIVES","count",d.get("openDrives")));return result;}
 @Transactional(readOnly=true)public PagedResponse<Map<String,Object>> activity(Pageable p){return PagedResponse.from(audits.findByInstitutionIdOrderByCreatedAtDesc(current.principal().institutionId(),p).map(a->Map.<String,Object>of("id",a.getId(),"action",a.getAction(),"resourceType",a.getResourceType(),"resourceId",Objects.toString(a.getResourceId(),""),"actor",a.getActor().getUsername(),"createdAt",a.getCreatedAt(),"details",a.getDetails())));}
}
