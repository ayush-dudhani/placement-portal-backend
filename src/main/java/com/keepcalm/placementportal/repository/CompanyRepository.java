package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.Company;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface CompanyRepository extends JpaRepository<Company,Long> {
 @Query("select c from Company c where c.institution.id=:iid and c.active=true and (:query is null or lower(c.name) like lower(concat('%',:query,'%')) or lower(c.industry) like lower(concat('%',:query,'%')))")
 Page<Company> search(@Param("iid") Long institutionId,@Param("query") String query,Pageable pageable);
 Optional<Company> findByIdAndInstitutionIdAndActiveTrue(Long id,Long institutionId);
 Optional<Company> findByIdAndInstitutionId(Long id,Long institutionId);
}
