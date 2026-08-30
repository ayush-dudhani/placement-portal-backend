package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.JobApplication;
import com.keepcalm.placementportal.enums.ApplicationStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface JobApplicationRepository extends JpaRepository<JobApplication,Long>{
 boolean existsByDriveRoleIdAndStudentId(Long roleId,Long studentId);
 Optional<JobApplication> findByStudentIdAndIdempotencyKey(Long studentId,String key);
 Optional<JobApplication> findByIdAndStudentId(Long id,Long studentId);
 Optional<JobApplication> findByIdAndInstitutionId(Long id,Long institutionId);
 @Query("select a from JobApplication a where a.student.id=:studentId and (:status is null or a.status=:status) and (:driveId is null or a.driveRole.drive.id=:driveId) and (:roleId is null or a.driveRole.id=:roleId)")
 Page<JobApplication> studentApplications(@Param("studentId")Long studentId,@Param("status")ApplicationStatus status,@Param("driveId")Long driveId,@Param("roleId")Long roleId,Pageable pageable);
 @Query("select a from JobApplication a where a.institution.id=:iid and (:status is null or a.status=:status) and (:driveId is null or a.driveRole.drive.id=:driveId) and (:roleId is null or a.driveRole.id=:roleId) and (:query is null or lower(a.student.firstName) like lower(concat('%',:query,'%')) or lower(a.student.rollNumber) like lower(concat('%',:query,'%')))")
 Page<JobApplication> adminSearch(@Param("iid")Long iid,@Param("status")ApplicationStatus status,@Param("driveId")Long driveId,@Param("roleId")Long roleId,@Param("query")String query,Pageable pageable);
 List<JobApplication> findByDriveRoleDriveIdAndInstitutionId(Long driveId,Long institutionId);
 List<JobApplication> findByStudentIdOrderByAppliedAtDesc(Long studentId);
}
