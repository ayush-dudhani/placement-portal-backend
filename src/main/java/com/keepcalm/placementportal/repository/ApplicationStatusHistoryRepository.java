package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.ApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory,Long>{ List<ApplicationStatusHistory> findByApplicationIdOrderByCreatedAtAsc(Long applicationId); }
