package com.keepcalm.placementportal.service;
import com.keepcalm.placementportal.api.PagedResponse;
import com.keepcalm.placementportal.entity.*;
import com.keepcalm.placementportal.enums.Role;
import com.keepcalm.placementportal.exception.DomainException;
import com.keepcalm.placementportal.models.company.CompanyDtos.*;
import com.keepcalm.placementportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor
public class CompanyService {
 private final CompanyRepository companies; private final CompanyFaqRepository faqs; private final CompanyQuestionRepository questions;
 private final QuestionReplyRepository replies; private final StudentRepository students; private final CurrentUserService current;
 private final PlacementOutcomeRepository outcomes;
 @Cacheable(cacheNames="companies",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true) public PagedResponse<CompanyDto> list(String query,Pageable pageable){ return PagedResponse.from(companies.search(current.principal().institutionId(),blank(query),pageable).map(this::dto)); }
 @Cacheable(cacheNames="companies",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true) public CompanyDto get(Long id){ return dto(company(id)); }
 @Cacheable(cacheNames="companies",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true) public List<FaqDto> faqs(Long id){ company(id); return faqs.findByCompanyIdOrderByDisplayOrderAsc(id).stream().map(f->new FaqDto(f.getId(),f.getQuestion(),f.getAnswer(),f.getDisplayOrder())).toList(); }
 @Cacheable(cacheNames="companies",keyGenerator="institutionAwareKeyGenerator",sync=true) @Transactional(readOnly=true) public PagedResponse<QuestionDto> questions(Long id,Pageable pageable){ company(id); return PagedResponse.from(questions.findByCompanyId(id,pageable).map(this::questionDto)); }
 @CacheEvict(cacheNames="companies",allEntries=true) @Transactional public QuestionDto ask(Long id,Ask request){ Company company=company(id); User user=current.getCurrentUser(); if(user.getRole()!=Role.STUDENT) throw DomainException.forbidden("Only students can ask company questions"); Student student=students.findByUserId(user.getId()).orElseThrow(()->DomainException.notFound("Student profile not found")); CompanyQuestion q=new CompanyQuestion(); q.setCompany(company);q.setStudent(student);q.setQuestion(request.question().trim());q.setAnonymous(request.anonymous()); return questionDto(questions.save(q)); }
 @CacheEvict(cacheNames="companies",allEntries=true) @Transactional public ReplyDto reply(Long companyId,Long questionId,Reply request){ company(companyId); CompanyQuestion q=questions.findById(questionId).filter(x->x.getCompany().getId().equals(companyId)).orElseThrow(()->DomainException.notFound("Question not found")); if(q.isClosed()) throw DomainException.conflict("QUESTION_CLOSED","Question is closed"); QuestionReply r=new QuestionReply();r.setQuestion(q);r.setAuthor(current.getCurrentUser());r.setBody(request.body().trim()); return replyDto(replies.save(r)); }
 @Cacheable(cacheNames="companies",keyGenerator="institutionAwareKeyGenerator",sync=true) public List<Map<String,Object>> outcomes(Long id){ company(id); return outcomes.findByCompanyIdAndCompanyInstitutionId(id,current.principal().institutionId()).stream().map(o->Map.<String,Object>of("id",o.getId(),"studentId",o.getStudent().getId(),"packageAmount",o.getPackageAmount(),"currency",o.getCurrency(),"placedAt",o.getPlacedAt())).toList(); }
 @Cacheable(cacheNames="companies",keyGenerator="institutionAwareKeyGenerator",sync=true) public List<Map<String,Object>> alumni(Long id){ company(id); return outcomes.findByCompanyIdAndCompanyInstitutionId(id,current.principal().institutionId()).stream().map(o->Map.<String,Object>of("studentId",o.getStudent().getId(),"name",Objects.toString(o.getStudent().getFirstName(),""),"branch",Objects.toString(o.getStudent().getBranch(),""),"graduationYear",Objects.toString(o.getStudent().getYearOfPassing(),""),"role",o.getOffer().getRoleTitle())).toList(); }
 private Company company(Long id){ return companies.findByIdAndInstitutionIdAndActiveTrue(id,current.principal().institutionId()).orElseThrow(()->DomainException.notFound("Company not found")); }
 private CompanyDto dto(Company c){return new CompanyDto(c.getId(),c.getName(),c.getIndustry(),c.getDescription(),c.getWebsiteUrl(),c.getLogoUrl(),c.getVersion());}
 private QuestionDto questionDto(CompanyQuestion q){ List<ReplyDto> rs=replies.findByQuestionIdOrderByCreatedAtAsc(q.getId()).stream().map(this::replyDto).toList(); String asker=q.isAnonymous()?"Anonymous":q.getStudent().getFirstName(); return new QuestionDto(q.getId(),q.getQuestion(),asker,q.isAnonymous(),q.isClosed(),q.getCreatedAt(),rs); }
 private ReplyDto replyDto(QuestionReply r){return new ReplyDto(r.getId(),r.getAuthor().getRole().name(),r.getBody(),r.getCreatedAt());}
 private String blank(String value){return value==null||value.isBlank()?null:value.trim();}
}
