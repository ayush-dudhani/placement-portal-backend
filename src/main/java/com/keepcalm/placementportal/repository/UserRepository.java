package com.keepcalm.placementportal.repository;

import com.keepcalm.placementportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByInstitutionIdAndUsernameIgnoreCase(Long institutionId, String username);

    Optional<User> findByInstitutionIdAndEmailIgnoreCase(Long institutionId, String email);

    boolean existsByInstitutionIdAndUsernameIgnoreCase(Long institutionId, String username);

    boolean existsByInstitutionIdAndEmailIgnoreCase(Long institutionId, String email);
}
