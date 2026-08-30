package com.keepcalm.placementportal.repository;

import com.keepcalm.placementportal.entity.Student;
import com.keepcalm.placementportal.entity.User;
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
