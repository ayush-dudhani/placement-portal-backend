package com.keepcalm.placementportal.repository.student;
import com.keepcalm.placementportal.repository.audit.*;
import com.keepcalm.placementportal.repository.communication.*;
import com.keepcalm.placementportal.repository.offer.*;
import com.keepcalm.placementportal.repository.selection.*;
import com.keepcalm.placementportal.repository.application.*;
import com.keepcalm.placementportal.repository.drive.*;
import com.keepcalm.placementportal.repository.company.*;
import com.keepcalm.placementportal.repository.profile.*;
import com.keepcalm.placementportal.repository.student.*;
import com.keepcalm.placementportal.repository.auth.*;
import com.keepcalm.placementportal.service.storage.*;
import com.keepcalm.placementportal.service.audit.*;
import com.keepcalm.placementportal.service.event.*;
import com.keepcalm.placementportal.service.analytics.*;
import com.keepcalm.placementportal.service.communication.*;
import com.keepcalm.placementportal.service.offer.*;
import com.keepcalm.placementportal.service.selection.*;
import com.keepcalm.placementportal.service.application.*;
import com.keepcalm.placementportal.service.drive.*;
import com.keepcalm.placementportal.service.company.*;
import com.keepcalm.placementportal.service.student.*;
import com.keepcalm.placementportal.service.profile.*;
import com.keepcalm.placementportal.service.auth.*;
import com.keepcalm.placementportal.entity.audit.*;
import com.keepcalm.placementportal.entity.communication.*;
import com.keepcalm.placementportal.entity.offer.*;
import com.keepcalm.placementportal.entity.selection.*;
import com.keepcalm.placementportal.entity.application.*;
import com.keepcalm.placementportal.entity.drive.*;
import com.keepcalm.placementportal.entity.company.*;
import com.keepcalm.placementportal.entity.profile.*;
import com.keepcalm.placementportal.entity.student.*;
import com.keepcalm.placementportal.entity.auth.*;
import com.keepcalm.placementportal.controller.event.*;
import com.keepcalm.placementportal.controller.analytics.*;
import com.keepcalm.placementportal.controller.communication.*;
import com.keepcalm.placementportal.controller.offer.*;
import com.keepcalm.placementportal.controller.selection.*;
import com.keepcalm.placementportal.controller.application.*;
import com.keepcalm.placementportal.controller.drive.*;
import com.keepcalm.placementportal.controller.company.*;
import com.keepcalm.placementportal.controller.student.*;
import com.keepcalm.placementportal.controller.profile.*;
import com.keepcalm.placementportal.controller.auth.*;

import com.keepcalm.placementportal.entity.student.Student;
import com.keepcalm.placementportal.entity.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository
        extends JpaRepository<Student, Long> {

    Optional<Student> findByUser(User user);

    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByIdAndInstitutionId(Long id, Long institutionId);

    boolean existsByInstitutionIdAndRollNumberIgnoreCase(Long institutionId, String rollNumber);

    @Query("select s from Student s where s.institution.id=:iid and (:query is null or lower(s.firstName) like lower(concat('%',:query,'%')) or lower(s.lastName) like lower(concat('%',:query,'%')) or lower(s.rollNumber) like lower(concat('%',:query,'%'))) and (:branch is null or s.branch=:branch) and (:year is null or s.yearOfPassing=:year) and (:status is null or s.placementStatus=:status)")
    Page<Student> adminSearch(@Param("iid")Long institutionId,@Param("query")String query,@Param("branch")String branch,@Param("year")Integer year,@Param("status")com.keepcalm.placementportal.enums.PlacementStatus status,Pageable pageable);
    long countByInstitutionId(Long institutionId);
}
