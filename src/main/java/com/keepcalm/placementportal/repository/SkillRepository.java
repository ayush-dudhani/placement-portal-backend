package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface SkillRepository extends JpaRepository<Skill, Long> { Optional<Skill> findByInstitutionIdAndNormalizedName(Long institutionId, String normalizedName); }
