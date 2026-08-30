package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.CompanyQuestion;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CompanyQuestionRepository extends JpaRepository<CompanyQuestion,Long>{ Page<CompanyQuestion> findByCompanyId(Long companyId,Pageable pageable); }
