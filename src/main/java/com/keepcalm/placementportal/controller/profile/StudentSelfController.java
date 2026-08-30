package com.keepcalm.placementportal.controller.profile;
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

import com.keepcalm.placementportal.enums.DocumentType;
import com.keepcalm.placementportal.models.student.*;
import com.keepcalm.placementportal.service.profile.DocumentService;
import com.keepcalm.placementportal.service.profile.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/students/me")
@RequiredArgsConstructor
public class StudentSelfController {
    private final StudentService students;
    private final DocumentService documents;

    @GetMapping("/profile") public StudentProfileDto profile() { return students.profile(); }
    @PutMapping("/profile") public StudentProfileDto updateProfile(@Valid @RequestBody StudentProfileDto request) { return students.updateProfile(request); }
    @GetMapping("/profile/completion") public ProfileCompletionDto completion() { return students.profileCompletion(); }
    @GetMapping("/dashboard") public Map<String, Object> dashboard() { return students.dashboard(); }
    @GetMapping("/eligibility-summary") public Map<String, Object> eligibility() { return students.eligibilitySummary(); }

    @GetMapping("/academics") public AcademicRecordDto academics() { return students.academics(); }
    @PutMapping("/academics") public AcademicRecordDto updateAcademics(@Valid @RequestBody AcademicRecordDto request) { return students.updateAcademics(request); }

    @GetMapping("/skills") public List<SkillDtos.SkillDto> skills() { return students.skills(); }
    @PutMapping("/skills") public List<SkillDtos.SkillDto> updateSkills(@Valid @RequestBody SkillDtos.Update request) { return students.updateSkills(request); }

    @GetMapping("/documents") public List<StudentDocumentDto> documents() { return documents.list(); }
    @PostMapping(value = "/documents", consumes = "multipart/form-data") @ResponseStatus(HttpStatus.CREATED)
    public StudentDocumentDto upload(@RequestParam DocumentType documentType, @RequestPart("file") MultipartFile file) { return documents.upload(documentType, file); }
    @GetMapping("/documents/{documentId}") public StudentDocumentDto document(@PathVariable Long documentId) { return documents.get(documentId); }
    @DeleteMapping("/documents/{documentId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable Long documentId) { documents.delete(documentId); }
    @PostMapping("/documents/{documentId}/set-primary") public StudentDocumentDto primary(@PathVariable Long documentId) { return documents.setPrimary(documentId); }
}
