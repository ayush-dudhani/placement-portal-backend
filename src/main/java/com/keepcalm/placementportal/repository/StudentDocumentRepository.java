package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.StudentDocument;
import com.keepcalm.placementportal.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Long> {
    List<StudentDocument> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    Optional<StudentDocument> findByIdAndStudentId(Long id, Long studentId);
    List<StudentDocument> findByStudentIdAndDocumentType(Long studentId, DocumentType type);
    Optional<StudentDocument> findByIdAndStudentIdAndStudentInstitutionId(Long id,Long studentId,Long institutionId);
    long countByStudentInstitutionIdAndVerificationStatus(Long institutionId,com.keepcalm.placementportal.enums.DocumentVerificationStatus status);
}
