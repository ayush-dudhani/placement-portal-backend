package com.keepcalm.placementportal.models.student;

import com.keepcalm.placementportal.enums.DocumentType;
import com.keepcalm.placementportal.enums.DocumentVerificationStatus;
import java.time.Instant;

public record StudentDocumentDto(Long id, DocumentType documentType, String originalFilename,
        String contentType, long sizeBytes, boolean primary, DocumentVerificationStatus verificationStatus,
        String verificationNote, Instant createdAt, long version) {
}
