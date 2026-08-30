package com.keepcalm.placementportal.controller;
import com.keepcalm.placementportal.models.company.CompanyDtos.*;
import com.keepcalm.placementportal.service.AdminCompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/admin/companies") @RequiredArgsConstructor
public class AdminCompanyController {
 private final AdminCompanyService service;
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public CompanyDto create(@Valid @RequestBody Upsert request){return service.create(request);}
 @PutMapping("/{companyId}") public CompanyDto update(@PathVariable Long companyId,@Valid @RequestBody Upsert request){return service.update(companyId,request);}
 @DeleteMapping("/{companyId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long companyId){service.archive(companyId);}
 @PostMapping("/{companyId}/contacts") @ResponseStatus(HttpStatus.CREATED) public ContactDto contact(@PathVariable Long companyId,@Valid @RequestBody ContactUpsert request){return service.addContact(companyId,request);}
 @PutMapping("/{companyId}/contacts/{contactId}") public ContactDto updateContact(@PathVariable Long companyId,@PathVariable Long contactId,@Valid @RequestBody ContactUpsert request){return service.updateContact(companyId,contactId,request);}
}
