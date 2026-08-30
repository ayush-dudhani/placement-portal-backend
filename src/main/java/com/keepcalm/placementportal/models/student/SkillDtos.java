package com.keepcalm.placementportal.models.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class SkillDtos {
    private SkillDtos() {}
    public record SkillDto(Long id, String name) {}
    public record Update(@Size(max = 50) List<@NotBlank @Size(max = 100) String> skills) {}
}
