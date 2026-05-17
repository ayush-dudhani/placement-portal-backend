package com.keepcalm.placementportal.repository;

import com.keepcalm.placementportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCollegeNameAndUsername(
            String collegeName,
            String username
    );

    boolean existsByCollegeNameAndUsername(
            String collegeName,
            String username
    );

    Optional<User> findByUsername(
            String username);
}