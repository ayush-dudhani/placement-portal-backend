package com.keepcalm.placementportal.service.profile;
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

import com.keepcalm.placementportal.entity.student.Student;
import com.keepcalm.placementportal.entity.profile.StudentDocument;
import com.keepcalm.placementportal.enums.DocumentType;
import com.keepcalm.placementportal.enums.DocumentVerificationStatus;
import com.keepcalm.placementportal.exception.DomainException;
import com.keepcalm.placementportal.models.student.StudentDocumentDto;
import com.keepcalm.placementportal.repository.profile.StudentDocumentRepository;
import com.keepcalm.placementportal.repository.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "image/png", "image/jpeg");
    private final StudentDocumentRepository documents;
    private final StudentRepository students;
    private final CurrentUserService currentUserService;
    private final ObjectStorageService storage;
    @Value("${storage.max-file-size:5242880}") private long maxFileSize;

    @Transactional(readOnly = true)
    public List<StudentDocumentDto> list() { return documents.findByStudentIdOrderByCreatedAtDesc(student().getId()).stream().map(this::dto).toList(); }

    @Transactional(readOnly = true)
    public StudentDocumentDto get(Long id) { return dto(owned(id)); }

    @CacheEvict(cacheNames = "analytics", allEntries = true)
    @Transactional
    public StudentDocumentDto upload(DocumentType type, MultipartFile file) {
        validate(file);
        Student student = student();
        StudentDocument document = new StudentDocument();
        document.setStudent(student);
        document.setDocumentType(type);
        document.setOriginalFilename(safeName(file.getOriginalFilename()));
        document.setContentType(Objects.requireNonNull(file.getContentType()));
        document.setSizeBytes(file.getSize());
        document.setObjectKey(storage.store(student.getInstitution().getId() + "/" + student.getId(), file));
        document.setVerificationStatus(DocumentVerificationStatus.PENDING);
        if (type == DocumentType.RESUME && documents.findByStudentIdAndDocumentType(student.getId(), type).isEmpty()) {
            document.setPrimaryDocument(true);
            student.setResumeUrl("/api/v1/students/me/documents/" + document.getId());
        }
        documents.save(document);
        if (document.isPrimaryDocument()) student.setResumeUrl("/api/v1/students/me/documents/" + document.getId());
        return dto(document);
    }

    @CacheEvict(cacheNames = "analytics", allEntries = true)
    @Transactional
    public void delete(Long id) {
        StudentDocument document = owned(id);
        if (document.getVerificationStatus() == DocumentVerificationStatus.VERIFIED) {
            throw DomainException.conflict("VERIFIED_DOCUMENT", "Verified documents cannot be deleted; contact an administrator");
        }
        documents.delete(document);
        storage.delete(document.getObjectKey());
    }

    @Transactional
    public StudentDocumentDto setPrimary(Long id) {
        StudentDocument selected = owned(id);
        if (selected.getDocumentType() != DocumentType.RESUME) {
            throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, "PRIMARY_MUST_BE_RESUME", "Only a resume can be primary");
        }
        documents.findByStudentIdAndDocumentType(selected.getStudent().getId(), DocumentType.RESUME)
                .forEach(document -> document.setPrimaryDocument(document.getId().equals(id)));
        selected.getStudent().setResumeUrl("/api/v1/students/me/documents/" + id);
        return dto(selected);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new DomainException(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "File is required");
        if (file.getSize() > maxFileSize) throw new DomainException(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "File exceeds the configured size limit");
        if (!ALLOWED_TYPES.contains(file.getContentType())) throw new DomainException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "INVALID_FILE_TYPE", "Unsupported document type");
        try {
            byte[] head = Arrays.copyOf(file.getBytes(), Math.min(8, (int) file.getSize()));
            boolean valid = switch (file.getContentType()) {
                case "application/pdf" -> starts(head, new byte[]{'%', 'P', 'D', 'F'});
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> starts(head, new byte[]{'P', 'K'});
                case "image/png" -> starts(head, new byte[]{(byte) 0x89, 'P', 'N', 'G'});
                case "image/jpeg" -> starts(head, new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff});
                default -> false;
            };
            if (!valid) throw new DomainException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "FILE_CONTENT_MISMATCH", "File content does not match its media type");
        } catch (IOException ex) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "INVALID_FILE", "File could not be inspected");
        }
    }

    private boolean starts(byte[] bytes, byte[] prefix) {
        if (bytes.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) if (bytes[i] != prefix[i]) return false;
        return true;
    }

    private String safeName(String name) { return name == null ? "document" : Path.of(name).getFileName().toString(); }
    private Student student() { return students.findByUserId(currentUserService.getCurrentUser().getId()).orElseThrow(() -> DomainException.notFound("Student profile not found")); }
    private StudentDocument owned(Long id) { Student student = student(); return documents.findByIdAndStudentId(id, student.getId()).orElseThrow(() -> DomainException.notFound("Document not found")); }
    private StudentDocumentDto dto(StudentDocument d) { return new StudentDocumentDto(d.getId(), d.getDocumentType(), d.getOriginalFilename(), d.getContentType(), d.getSizeBytes(), d.isPrimaryDocument(), d.getVerificationStatus(), d.getVerificationNote(), d.getCreatedAt(), d.getVersion()); }
}
