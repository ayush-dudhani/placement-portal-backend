package com.keepcalm.placementportal.repository;

import com.keepcalm.placementportal.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByCodeIgnoreCaseAndActiveTrue(String code);
}
