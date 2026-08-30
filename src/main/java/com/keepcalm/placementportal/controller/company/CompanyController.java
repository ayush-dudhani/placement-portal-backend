package com.keepcalm.placementportal.controller.company;
import com.keepcalm.placementportal.repository.audit.*;
import com.keepcalm.placementportal.repository.communication.*;
import com.keepcalm.placementportal.repository.offer.*;
import com.keepcalm.placementportal.repository.selection.*;
import com.keepcalm.placementportal.repository.application.*;
import com.keepcalm.placementportal.repository.drive.*;
import com.keepcalm.placementportal.repository.company.*;
import com.keepcalm.placementportal.repository.profile.*;
import com.keepcalm.placementportal.repository.student.*;
import com.keepcalm.placementportal.repository.auth.*;
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
import com.keepcalm.placementportal.entity.audit.*;
import com.keepcalm.placementportal.entity.communication.*;
import com.keepcalm.placementportal.entity.offer.*;
import com.keepcalm.placementportal.entity.selection.*;
import com.keepcalm.placementportal.entity.application.*;
import com.keepcalm.placementportal.entity.drive.*;
import com.keepcalm.placementportal.entity.company.*;
import com.keepcalm.placementportal.entity.profile.*;
import com.keepcalm.placementportal.entity.student.*;
import com.keepcalm.placementportal.entity.auth.*;
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
import com.keepcalm.placementportal.models.company.CompanyDtos.*;
import com.keepcalm.placementportal.service.company.CompanyService;
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
