package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.EligibilityRule;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface EligibilityRuleRepository extends JpaRepository<EligibilityRule,Long>{
 @Query("select r from EligibilityRule r where r.drive.id=:driveId and r.active=true and (r.driveRole is null or r.driveRole.id=:roleId) order by r.id")
 List<EligibilityRule> applicable(@Param("driveId")Long driveId,@Param("roleId")Long roleId);
 long countByDriveIdAndActiveTrue(Long driveId);
 List<EligibilityRule> findByDriveId(Long driveId);
}
