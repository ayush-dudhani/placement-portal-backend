package com.keepcalm.placementportal.controller.drive;
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
import com.keepcalm.placementportal.models.application.ApplicationDtos.ApplicationDto;
import com.keepcalm.placementportal.models.drive.DriveDtos.*;
import com.keepcalm.placementportal.service.auth.*;
import com.keepcalm.placementportal.service.profile.*;
import com.keepcalm.placementportal.service.student.*;
import com.keepcalm.placementportal.service.company.*;
import com.keepcalm.placementportal.service.drive.*;
import com.keepcalm.placementportal.service.application.*;
import com.keepcalm.placementportal.service.selection.*;
import com.keepcalm.placementportal.service.offer.*;
import com.keepcalm.placementportal.service.communication.*;
import com.keepcalm.placementportal.service.analytics.*;
import com.keepcalm.placementportal.service.event.*;
import com.keepcalm.placementportal.service.audit.*;
import com.keepcalm.placementportal.service.storage.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/drives") @RequiredArgsConstructor
public class DriveController {
 private final DriveDiscoveryService drives;private final ApplicationService applications;
 @GetMapping public PagedResponse<DriveDto> list(@RequestParam(required=false)String query,@RequestParam(required=false)Long companyId,@PageableDefault(size=20,sort="applicationDeadline")Pageable pageable){return drives.list(query,companyId,pageable);}
 @GetMapping("/{driveId}") public DriveDto get(@PathVariable Long driveId){return drives.get(driveId);}
 @GetMapping("/{driveId}/roles") public List<RoleDto> roles(@PathVariable Long driveId){return drives.roles(driveId);}
 @GetMapping("/{driveId}/roles/{roleId}") public RoleDto role(@PathVariable Long driveId,@PathVariable Long roleId){return drives.role(driveId,roleId);}
 @GetMapping("/{driveId}/roles/{roleId}/eligibility") public EligibilityDto eligibility(@PathVariable Long driveId,@PathVariable Long roleId){return drives.eligibility(driveId,roleId);}
 @PostMapping("/{driveId}/roles/{roleId}/applications") @ResponseStatus(HttpStatus.CREATED)
 public ApplicationDto apply(@PathVariable Long driveId,@PathVariable Long roleId,@RequestHeader(name="Idempotency-Key",required=false)String key){return applications.apply(driveId,roleId,key);}
}
