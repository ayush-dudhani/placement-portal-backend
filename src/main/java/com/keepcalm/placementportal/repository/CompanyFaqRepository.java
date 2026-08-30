package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.CompanyFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CompanyFaqRepository extends JpaRepository<CompanyFaq,Long>{ List<CompanyFaq> findByCompanyIdOrderByDisplayOrderAsc(Long companyId); }
