package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.AuditLog;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditLogRepository extends JpaRepository<AuditLog,Long>{Page<AuditLog> findByInstitutionIdOrderByCreatedAtDesc(Long institutionId,Pageable pageable);}
