package com.keepcalm.placementportal.controller;
import com.keepcalm.placementportal.api.PagedResponse;
import com.keepcalm.placementportal.enums.*;
import com.keepcalm.placementportal.models.application.ApplicationDtos.ApplicationDto;
import com.keepcalm.placementportal.models.drive.DriveDtos.*;
import com.keepcalm.placementportal.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/admin/drives") @RequiredArgsConstructor
public class AdminDriveController {
 private final AdminDriveService drives;private final AdminApplicationService applications;
 @GetMapping public PagedResponse<DriveDto> list(@RequestParam(required=false)String query,@RequestParam(required=false)DriveStatus status,@RequestParam(required=false)Long companyId,@PageableDefault(size=20,sort="createdAt",direction=Sort.Direction.DESC)Pageable p){return drives.list(query,status,companyId,p);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public DriveDto create(@Valid @RequestBody DriveUpsert r){return drives.create(r);}
 @GetMapping("/{driveId}") public DriveDto get(@PathVariable Long driveId){return drives.get(driveId);}
 @PutMapping("/{driveId}") public DriveDto update(@PathVariable Long driveId,@Valid @RequestBody DriveUpsert r){return drives.update(driveId,r);}
 @DeleteMapping("/{driveId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long driveId){drives.archive(driveId);}
 @PostMapping("/{driveId}/publish") public DriveDto publish(@PathVariable Long driveId){return drives.publish(driveId);}
 @PostMapping("/{driveId}/close-applications") public DriveDto close(@PathVariable Long driveId){return drives.close(driveId);}
 @PostMapping("/{driveId}/cancel") public DriveDto cancel(@PathVariable Long driveId){return drives.cancel(driveId);}
 @PostMapping("/{driveId}/clone") @ResponseStatus(HttpStatus.CREATED) public DriveDto cloneDrive(@PathVariable Long driveId){return drives.cloneDrive(driveId);}
 @PostMapping("/{driveId}/roles") @ResponseStatus(HttpStatus.CREATED) public RoleDto role(@PathVariable Long driveId,@Valid @RequestBody RoleUpsert r){return drives.addRole(driveId,r);}
 @PutMapping("/{driveId}/roles/{roleId}") public RoleDto role(@PathVariable Long driveId,@PathVariable Long roleId,@Valid @RequestBody RoleUpsert r){return drives.updateRole(driveId,roleId,r);}
 @DeleteMapping("/{driveId}/roles/{roleId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteRole(@PathVariable Long driveId,@PathVariable Long roleId){drives.deleteRole(driveId,roleId);}
 @PutMapping("/{driveId}/eligibility-rules") public List<RuleDto> rules(@PathVariable Long driveId,@Valid @RequestBody RulesUpdate r){return drives.updateRules(driveId,r);}
 @GetMapping("/{driveId}/eligibility-preview") public Map<String,Object> preview(@PathVariable Long driveId){return drives.preview(driveId);}
 @GetMapping("/{driveId}/applicants") public PagedResponse<ApplicationDto> applicants(@PathVariable Long driveId,@RequestParam(required=false)ApplicationStatus status,@RequestParam(required=false)String query,@PageableDefault(size=20)Pageable p){return applications.list(status,driveId,null,query,p);}
 @GetMapping(value="/{driveId}/applicants/export",produces="text/csv") public ResponseEntity<byte[]> export(@PathVariable Long driveId,@RequestParam(required=false)ApplicationStatus status,@RequestParam(required=false)String query){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=drive-"+driveId+"-applicants.csv").body(applications.export(driveId,status,query));}
}
