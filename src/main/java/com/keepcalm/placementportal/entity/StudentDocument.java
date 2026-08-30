package com.keepcalm.placementportal.entity;

import com.keepcalm.placementportal.enums.DocumentType;
import com.keepcalm.placementportal.enums.DocumentVerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_documents", indexes = {
        @Index(name = "idx_documents_student", columnList = "student_id"),
        @Index(name = "idx_documents_verification", columnList = "verification_status")})
@Getter @Setter @NoArgsConstructor
public class StudentDocument extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "student_id", nullable = false) private Student student;
    @Enumerated(EnumType.STRING) @Column(name = "document_type", nullable = false, length = 40) private DocumentType documentType;
    @Column(name = "object_key", nullable = false, unique = true, length = 500) private String objectKey;
    @Column(name = "original_filename", nullable = false, length = 255) private String originalFilename;
    @Column(name = "content_type", nullable = false, length = 100) private String contentType;
    @Column(name = "size_bytes", nullable = false) private long sizeBytes;
    @Column(name = "is_primary", nullable = false) private boolean primaryDocument;
    @Enumerated(EnumType.STRING) @Column(name = "verification_status", nullable = false, length = 20)
    private DocumentVerificationStatus verificationStatus = DocumentVerificationStatus.PENDING;
    @Column(name = "verification_note", length = 500) private String verificationNote;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "verified_by") private User verifiedBy;
}
