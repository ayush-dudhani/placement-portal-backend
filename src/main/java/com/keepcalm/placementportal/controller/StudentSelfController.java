package com.keepcalm.placementportal.controller;

import com.keepcalm.placementportal.enums.DocumentType;
import com.keepcalm.placementportal.models.student.*;
import com.keepcalm.placementportal.service.DocumentService;
import com.keepcalm.placementportal.service.StudentService;
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
