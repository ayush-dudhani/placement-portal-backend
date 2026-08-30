package com.keepcalm.placementportal.service;

import com.keepcalm.placementportal.entity.*;
import com.keepcalm.placementportal.enums.DocumentType;
import com.keepcalm.placementportal.exception.DomainException;
import com.keepcalm.placementportal.models.student.*;
import com.keepcalm.placementportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository students;
    private final AcademicRecordRepository academics;
    private final SkillRepository skills;
    private final StudentSkillRepository studentSkills;
    private final StudentDocumentRepository documents;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public StudentProfileDto profile() { return profileDto(student()); }

    @Transactional
    public StudentProfileDto updateProfile(StudentProfileDto request) {
        Student student = student();
        if (request.rollNumber() != null && !request.rollNumber().equalsIgnoreCase(student.getRollNumber())
                && students.existsByInstitutionIdAndRollNumberIgnoreCase(student.getInstitution().getId(), request.rollNumber())) {
            throw DomainException.conflict("ROLL_NUMBER_EXISTS", "Roll number already exists in this institution");
        }
        student.setFirstName(trim(request.firstName()));
        student.setLastName(trim(request.lastName()));
        student.setMobileNo(trim(request.mobileNo()));
        student.setRollNumber(trim(request.rollNumber()));
        student.setDateOfBirth(request.dateOfBirth());
        student.setGender(trim(request.gender()));
        student.setLinkedinUrl(trim(request.linkedinUrl()));
        student.setGithubUrl(trim(request.githubUrl()));
        student.setCollegeName(student.getInstitution().getName());
        student.setProfileCompletion(completion(student).percentage());
        return profileDto(student);
    }

    @Transactional
    public AcademicRecordDto academics() { return academicDto(record(student())); }

    @Transactional
    public AcademicRecordDto updateAcademics(AcademicRecordDto request) {
        Student student = student();
        AcademicRecord record = record(student);
        record.setCgpa(request.cgpa());
        record.setTenthPercentage(request.tenthPercentage());
        record.setTwelfthPercentage(request.twelfthPercentage());
        record.setDiplomaPercentage(request.diplomaPercentage());
        record.setActiveBacklogs(request.activeBacklogs());
        record.setBranch(request.branch().trim());
        record.setGraduationYear(request.graduationYear());
        syncLegacyAcademics(student, record);
        academics.save(record);
        student.setProfileCompletion(completion(student).percentage());
        return academicDto(record);
    }

    @Transactional(readOnly = true)
    public List<SkillDtos.SkillDto> skills() {
        return studentSkills.findByStudentIdOrderBySkillNameAsc(student().getId()).stream()
                .map(link -> new SkillDtos.SkillDto(link.getSkill().getId(), link.getSkill().getName())).toList();
    }

    @Transactional
    public List<SkillDtos.SkillDto> updateSkills(SkillDtos.Update request) {
        Student student = student();
        studentSkills.deleteByStudentId(student.getId());
        LinkedHashMap<String, String> distinct = new LinkedHashMap<>();
        if (request.skills() != null) request.skills().forEach(name -> distinct.putIfAbsent(normalize(name), name.trim()));
        for (Map.Entry<String, String> item : distinct.entrySet()) {
            Skill skill = skills.findByInstitutionIdAndNormalizedName(student.getInstitution().getId(), item.getKey()).orElseGet(() -> {
                Skill created = new Skill(); created.setInstitution(student.getInstitution()); created.setName(item.getValue()); created.setNormalizedName(item.getKey());
                return skills.save(created);
            });
            StudentSkill link = new StudentSkill(); link.setStudent(student); link.setSkill(skill); studentSkills.save(link);
        }
        student.setSkills(new ArrayList<>(distinct.values()));
        return studentSkills.findByStudentIdOrderBySkillNameAsc(student.getId()).stream()
                .map(link -> new SkillDtos.SkillDto(link.getSkill().getId(), link.getSkill().getName())).toList();
    }

    @Transactional(readOnly = true)
    public ProfileCompletionDto profileCompletion() { return completion(student()); }

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard() {
        Student student = student();
        return Map.of("profileCompletion", completion(student).percentage(), "placementStatus", student.getPlacementStatus().name(),
                "documents", documents.findByStudentIdOrderByCreatedAtDesc(student.getId()).size(), "applications", 0, "upcomingEvents", 0);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> eligibilitySummary() {
        Student student = student();
        AcademicRecord record = academics.findByStudentId(student.getId()).orElse(null);
        boolean complete = record != null && record.getCgpa() != null && student.getRollNumber() != null;
        return Map.of("profileReady", complete, "cgpa", record == null || record.getCgpa() == null ? "" : record.getCgpa(),
                "activeBacklogs", record == null ? 0 : record.getActiveBacklogs(), "message", complete ? "Profile is ready for drive-specific eligibility checks" : "Complete profile and academic details");
    }

    private Student student() {
        User user = currentUserService.getCurrentUser();
        return students.findByUserId(user.getId()).orElseThrow(() -> DomainException.notFound("Student profile not found"));
    }

    private AcademicRecord record(Student student) {
        return academics.findByStudentId(student.getId()).orElseGet(() -> {
            AcademicRecord record = new AcademicRecord(); record.setStudent(student);
            record.setCgpa(student.getCgpa()); record.setTenthPercentage(student.getTenthPercentage()); record.setTwelfthPercentage(student.getTwelfthPercentage());
            record.setDiplomaPercentage(student.getDiplomaPercentage()); record.setActiveBacklogs(student.getActiveBacklogs() == null ? 0 : student.getActiveBacklogs());
            record.setBranch(student.getBranch() == null ? "UNSPECIFIED" : student.getBranch()); record.setGraduationYear(student.getYearOfPassing() == null ? java.time.Year.now().getValue() : student.getYearOfPassing());
            return academics.save(record);
        });
    }

    private void syncLegacyAcademics(Student s, AcademicRecord a) {
        s.setCgpa(a.getCgpa()); s.setTenthPercentage(a.getTenthPercentage()); s.setTwelfthPercentage(a.getTwelfthPercentage()); s.setDiplomaPercentage(a.getDiplomaPercentage());
        s.setActiveBacklogs(a.getActiveBacklogs()); s.setBranch(a.getBranch()); s.setYearOfPassing(a.getGraduationYear());
    }

    private ProfileCompletionDto completion(Student s) {
        List<String> done = new ArrayList<>(), missing = new ArrayList<>();
        section("personal", s.getFirstName() != null && s.getMobileNo() != null && s.getDateOfBirth() != null, done, missing);
        section("identity", s.getRollNumber() != null, done, missing);
        section("academics", academics.findByStudentId(s.getId()).map(a -> a.getCgpa() != null && a.getBranch() != null && a.getGraduationYear() > 0).orElse(false), done, missing);
        section("skills", !studentSkills.findByStudentIdOrderBySkillNameAsc(s.getId()).isEmpty(), done, missing);
        section("resume", !documents.findByStudentIdAndDocumentType(s.getId(), DocumentType.RESUME).isEmpty(), done, missing);
        return new ProfileCompletionDto(done.size() * 100 / 5, done, missing);
    }

    private void section(String name, boolean complete, List<String> done, List<String> missing) { (complete ? done : missing).add(name); }
    private StudentProfileDto profileDto(Student s) { return new StudentProfileDto(s.getId(), s.getInstitution().getName(), s.getFirstName(), s.getLastName(), s.getMobileNo(), s.getRollNumber(), s.getDateOfBirth(), s.getGender(), s.getLinkedinUrl(), s.getGithubUrl(), s.getPlacementStatus().name(), completion(s).percentage(), s.getVersion()); }
    private AcademicRecordDto academicDto(AcademicRecord a) { return new AcademicRecordDto(a.getId(), a.getCgpa(), a.getTenthPercentage(), a.getTwelfthPercentage(), a.getDiplomaPercentage(), a.getActiveBacklogs(), a.getBranch(), a.getGraduationYear()); }
    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String normalize(String value) { return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " "); }
}
