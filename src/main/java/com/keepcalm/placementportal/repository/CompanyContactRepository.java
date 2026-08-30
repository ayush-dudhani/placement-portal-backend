package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.CompanyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CompanyContactRepository extends JpaRepository<CompanyContact,Long>{ List<CompanyContact> findByCompanyIdOrderByPrimaryContactDescNameAsc(Long companyId); Optional<CompanyContact> findByIdAndCompanyId(Long id,Long companyId); }
