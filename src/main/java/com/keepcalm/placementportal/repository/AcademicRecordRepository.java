package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.AcademicRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, Long> { Optional<AcademicRecord> findByStudentId(Long studentId); java.util.List<AcademicRecord> findByStudentInstitutionId(Long institutionId); }
