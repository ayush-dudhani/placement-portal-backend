package com.keepcalm.placementportal.controller;
import com.keepcalm.placementportal.api.PagedResponse;
import com.keepcalm.placementportal.models.application.ApplicationDtos.ApplicationDto;
import com.keepcalm.placementportal.models.drive.DriveDtos.*;
import com.keepcalm.placementportal.service.*;
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
