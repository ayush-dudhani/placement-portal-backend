package com.keepcalm.placementportal.models.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StudentProfileDto(Long id, String institutionName,
        @NotBlank @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Pattern(regexp = "^[+0-9() -]{7,20}$") String mobileNo,
        @NotBlank @Size(max = 50) String rollNumber,
        @Past LocalDate dateOfBirth,
        @Size(max = 30) String gender,
        @Size(max = 500) String linkedinUrl,
        @Size(max = 500) String githubUrl,
        String placementStatus, int profileCompletion, long version) {
}
