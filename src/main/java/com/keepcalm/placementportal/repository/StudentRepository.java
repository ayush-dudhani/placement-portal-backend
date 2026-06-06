package com.keepcalm.placementportal.repository;

import com.keepcalm.placementportal.entity.Student;
import com.keepcalm.placementportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository
        extends JpaRepository<Student, Long> {

    Optional<Student> findByUser(User user);

    Optional<Student> findByUserId(Long userId);
}
