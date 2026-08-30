package com.keepcalm.placementportal.controller;
import com.keepcalm.placementportal.api.PagedResponse;
import com.keepcalm.placementportal.enums.ApplicationStatus;
import com.keepcalm.placementportal.models.application.ApplicationDtos.*;
import com.keepcalm.placementportal.service.AdminApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
@RestController @RequestMapping("/api/v1/admin/applications") @RequiredArgsConstructor
public class AdminApplicationController {
 private final AdminApplicationService service;
 @GetMapping public PagedResponse<ApplicationDto> list(@RequestParam(required=false)ApplicationStatus status,@RequestParam(required=false)Long driveId,@RequestParam(required=false)Long roleId,@RequestParam(required=false)String query,@PageableDefault(size=20,sort="appliedAt",direction=Sort.Direction.DESC)Pageable p){return service.list(status,driveId,roleId,query,p);}
 @GetMapping("/{applicationId}") public ApplicationDto get(@PathVariable Long applicationId){return service.get(applicationId);}
 @PatchMapping("/{applicationId}/status") public ApplicationDto status(@PathVariable Long applicationId,@Valid @RequestBody StatusUpdate r){return service.status(applicationId,r);}
 @PostMapping("/bulk-eligibility-check") public List<Map<String,Object>> eligibility(@Valid @RequestBody BulkIds r){return service.bulkEligibility(r);}
 @PostMapping("/bulk-status-update") public Map<String,Object> bulkStatus(@Valid @RequestBody BulkStatusUpdate r){return service.bulkStatus(r);}
 @PostMapping(value="/import-results",consumes="multipart/form-data") public Map<String,Object> importResults(@RequestPart("file")MultipartFile file,@RequestHeader("Idempotency-Key")String key){return service.importResults(file,key);}
}
