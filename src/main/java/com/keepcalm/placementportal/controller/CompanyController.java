package com.keepcalm.placementportal.controller;
import com.keepcalm.placementportal.api.PagedResponse;
import com.keepcalm.placementportal.models.company.CompanyDtos.*;
import com.keepcalm.placementportal.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/companies") @RequiredArgsConstructor
public class CompanyController {
 private final CompanyService service;
 @GetMapping public PagedResponse<CompanyDto> list(@RequestParam(required=false)String query,@PageableDefault(size=20,sort="name")Pageable pageable){return service.list(query,pageable);}
 @GetMapping("/{companyId}") public CompanyDto get(@PathVariable Long companyId){return service.get(companyId);}
 @GetMapping("/{companyId}/outcomes") public List<Map<String,Object>> outcomes(@PathVariable Long companyId){return service.outcomes(companyId);}
 @GetMapping("/{companyId}/alumni") public List<Map<String,Object>> alumni(@PathVariable Long companyId){return service.alumni(companyId);}
 @GetMapping("/{companyId}/faqs") public List<FaqDto> faqs(@PathVariable Long companyId){return service.faqs(companyId);}
 @GetMapping("/{companyId}/questions") public PagedResponse<QuestionDto> questions(@PathVariable Long companyId,@PageableDefault(size=20)Pageable pageable){return service.questions(companyId,pageable);}
 @PostMapping("/{companyId}/questions") public QuestionDto ask(@PathVariable Long companyId,@Valid @RequestBody Ask request){return service.ask(companyId,request);}
 @PostMapping("/{companyId}/questions/{questionId}/replies") public ReplyDto reply(@PathVariable Long companyId,@PathVariable Long questionId,@Valid @RequestBody Reply request){return service.reply(companyId,questionId,request);}
}
