package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.StudentSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface StudentSkillRepository extends JpaRepository<StudentSkill, Long> {
    List<StudentSkill> findByStudentIdOrderBySkillNameAsc(Long studentId);
    void deleteByStudentId(Long studentId);
}
