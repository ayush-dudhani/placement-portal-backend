package com.keepcalm.placementportal.controller;
import com.keepcalm.placementportal.api.PagedResponse;
import com.keepcalm.placementportal.enums.ApplicationStatus;
import com.keepcalm.placementportal.models.application.ApplicationDtos.*;
import com.keepcalm.placementportal.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/students/me/applications") @RequiredArgsConstructor
public class StudentApplicationController {
 private final ApplicationService service;
 @GetMapping public PagedResponse<ApplicationDto> list(@RequestParam(required=false)ApplicationStatus status,@RequestParam(required=false)Long driveId,@RequestParam(required=false)Long roleId,@PageableDefault(size=20,sort="appliedAt",direction=Sort.Direction.DESC)Pageable pageable){return service.list(status,driveId,roleId,pageable);}
 @GetMapping("/{applicationId}") public ApplicationDto get(@PathVariable Long applicationId){return service.get(applicationId);}
 @GetMapping("/{applicationId}/history") public List<HistoryDto> history(@PathVariable Long applicationId){return service.history(applicationId);}
 @PostMapping("/{applicationId}/withdraw") public ApplicationDto withdraw(@PathVariable Long applicationId,@Valid @RequestBody(required=false)Withdraw request){return service.withdraw(applicationId,request);}
}
