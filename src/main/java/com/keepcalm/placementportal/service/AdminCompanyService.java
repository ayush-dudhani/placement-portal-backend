package com.keepcalm.placementportal.service;
import com.keepcalm.placementportal.entity.*;
import com.keepcalm.placementportal.exception.DomainException;
import com.keepcalm.placementportal.models.company.CompanyDtos.*;
import com.keepcalm.placementportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
@Service @RequiredArgsConstructor
public class AdminCompanyService {
 private final CompanyRepository companies;private final CompanyContactRepository contacts;private final CurrentUserService current;private final AuditService audit;
 @CacheEvict(cacheNames={"companies","analytics"},allEntries=true) @Transactional public CompanyDto create(Upsert r){Company c=new Company();c.setInstitution(current.getCurrentUser().getInstitution());apply(c,r);companies.save(c);audit.record("COMPANY_CREATED","Company",c.getId(),Map.of("name",c.getName()));return dto(c);}
 @CacheEvict(cacheNames={"companies","analytics"},allEntries=true) @Transactional public CompanyDto update(Long id,Upsert r){Company c=company(id);apply(c,r);audit.record("COMPANY_UPDATED","Company",id,Map.of());return dto(c);}
 @CacheEvict(cacheNames={"companies","analytics"},allEntries=true) @Transactional public void archive(Long id){Company c=company(id);c.setActive(false);audit.record("COMPANY_ARCHIVED","Company",id,Map.of());}
 @CacheEvict(cacheNames="companies",allEntries=true) @Transactional public ContactDto addContact(Long companyId,ContactUpsert r){Company c=company(companyId);if(r.primary())contacts.findByCompanyIdOrderByPrimaryContactDescNameAsc(companyId).forEach(x->x.setPrimaryContact(false));CompanyContact x=new CompanyContact();x.setCompany(c);apply(x,r);contacts.save(x);audit.record("COMPANY_CONTACT_CREATED","Company",companyId,Map.of("contactId",x.getId()));return dto(x);}
 @CacheEvict(cacheNames="companies",allEntries=true) @Transactional public ContactDto updateContact(Long companyId,Long contactId,ContactUpsert r){company(companyId);CompanyContact x=contacts.findByIdAndCompanyId(contactId,companyId).orElseThrow(()->DomainException.notFound("Contact not found"));if(r.primary())contacts.findByCompanyIdOrderByPrimaryContactDescNameAsc(companyId).forEach(c->c.setPrimaryContact(c.getId().equals(contactId)));apply(x,r);audit.record("COMPANY_CONTACT_UPDATED","Company",companyId,Map.of("contactId",contactId));return dto(x);}
 private void apply(Company c,Upsert r){c.setName(r.name().trim());c.setIndustry(r.industry());c.setDescription(r.description());c.setWebsiteUrl(r.websiteUrl());c.setLogoUrl(r.logoUrl());c.setActive(true);}
 private void apply(CompanyContact c,ContactUpsert r){c.setName(r.name().trim());c.setEmail(r.email().trim().toLowerCase());c.setPhone(r.phone());c.setDesignation(r.designation());c.setPrimaryContact(r.primary());}
 private Company company(Long id){return companies.findByIdAndInstitutionId(id,current.principal().institutionId()).orElseThrow(()->DomainException.notFound("Company not found"));}
 private CompanyDto dto(Company c){return new CompanyDto(c.getId(),c.getName(),c.getIndustry(),c.getDescription(),c.getWebsiteUrl(),c.getLogoUrl(),c.getVersion());}
 private ContactDto dto(CompanyContact c){return new ContactDto(c.getId(),c.getName(),c.getEmail(),c.getPhone(),c.getDesignation(),c.isPrimaryContact(),c.getVersion());}
}
