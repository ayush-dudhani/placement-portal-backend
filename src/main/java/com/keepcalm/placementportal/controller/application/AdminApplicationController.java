package com.keepcalm.placementportal.controller.application;
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
import com.keepcalm.placementportal.enums.ApplicationStatus;
import com.keepcalm.placementportal.models.application.ApplicationDtos.*;
import com.keepcalm.placementportal.service.application.AdminApplicationService;
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
