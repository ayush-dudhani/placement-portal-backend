package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.CampusDrive;
import com.keepcalm.placementportal.enums.DriveStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface CampusDriveRepository extends JpaRepository<CampusDrive,Long>{
 @Query("select d from CampusDrive d where d.institution.id=:iid and d.archived=false and d.status in :statuses and (:companyId is null or d.company.id=:companyId) and (:query is null or lower(d.title) like lower(concat('%',:query,'%')) or lower(d.company.name) like lower(concat('%',:query,'%')))")
 Page<CampusDrive> discover(@Param("iid")Long iid,@Param("statuses")Collection<DriveStatus>statuses,@Param("companyId")Long companyId,@Param("query")String query,Pageable pageable);
 Optional<CampusDrive> findByIdAndInstitutionId(Long id,Long institutionId);
 @Query("select d from CampusDrive d where d.institution.id=:iid and (:status is null or d.status=:status) and (:companyId is null or d.company.id=:companyId) and (:query is null or lower(d.title) like lower(concat('%',:query,'%')))")
 Page<CampusDrive> adminSearch(@Param("iid")Long iid,@Param("status")DriveStatus status,@Param("companyId")Long companyId,@Param("query")String query,Pageable pageable);
}
