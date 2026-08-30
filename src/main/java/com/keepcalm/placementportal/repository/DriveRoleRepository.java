package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.DriveRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface DriveRoleRepository extends JpaRepository<DriveRole,Long>{ List<DriveRole> findByDriveIdAndActiveTrueOrderByTitleAsc(Long driveId); Optional<DriveRole> findByIdAndDriveIdAndActiveTrue(Long id,Long driveId); Optional<DriveRole> findByIdAndDriveInstitutionId(Long id,Long institutionId); long countByDriveIdAndActiveTrue(Long driveId); }
