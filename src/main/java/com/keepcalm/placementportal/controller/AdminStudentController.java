package com.keepcalm.placementportal.controller;
import com.keepcalm.placementportal.api.PagedResponse;
import com.keepcalm.placementportal.enums.PlacementStatus;
import com.keepcalm.placementportal.models.admin.AdminStudentDtos.*;
import com.keepcalm.placementportal.models.application.ApplicationDtos.ApplicationDto;
import com.keepcalm.placementportal.models.student.StudentDocumentDto;
import com.keepcalm.placementportal.service.AdminStudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
@RestController @RequestMapping("/api/v1/admin/students") @RequiredArgsConstructor
public class AdminStudentController {
 private final AdminStudentService service;
 @GetMapping public PagedResponse<StudentDto> list(@RequestParam(required=false)String query,@RequestParam(required=false)String branch,@RequestParam(required=false)Integer graduationYear,@RequestParam(required=false)PlacementStatus status,@PageableDefault(size=20,sort="id")Pageable p){return service.list(query,branch,graduationYear,status,p);}
 @GetMapping("/{studentId}") public StudentDto get(@PathVariable Long studentId){return service.get(studentId);}
 @PatchMapping("/{studentId}/placement-status") public StudentDto placement(@PathVariable Long studentId,@Valid @RequestBody PlacementUpdate r){return service.placement(studentId,r);}
 @GetMapping("/{studentId}/applications") public List<ApplicationDto> applications(@PathVariable Long studentId){return service.applications(studentId);}
 @GetMapping("/{studentId}/documents") public List<StudentDocumentDto> documents(@PathVariable Long studentId){return service.documents(studentId);}
 @PatchMapping("/{studentId}/documents/{documentId}/verification") public StudentDocumentDto verify(@PathVariable Long studentId,@PathVariable Long documentId,@Valid @RequestBody VerificationUpdate r){return service.verify(studentId,documentId,r);}
 @GetMapping(value="/export",produces="text/csv") public ResponseEntity<byte[]> export(@RequestParam(required=false)String query,@RequestParam(required=false)String branch,@RequestParam(required=false)Integer graduationYear,@RequestParam(required=false)PlacementStatus status){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=students.csv").body(service.export(query,branch,graduationYear,status));}
 @PostMapping(value="/import",consumes="multipart/form-data") public Map<String,Object> importStudents(@RequestPart("file")MultipartFile file,@RequestHeader("Idempotency-Key")String key){return service.importStudents(file,key);}
}
