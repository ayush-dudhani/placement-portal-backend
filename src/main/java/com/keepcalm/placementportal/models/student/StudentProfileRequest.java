package com.keepcalm.placementportal.models.student;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentProfileRequest {

    private String firstName;
    private String lastName;

    private String mobileNo;

    private String rollNumber;

    private String branch;
    private Integer yearOfPassing;

    private BigDecimal cgpa;

    private BigDecimal tenthPercentage;
    private BigDecimal twelfthPercentage;
    private BigDecimal diplomaPercentage;

    private Integer activeBacklogs;

    private String resumeUrl;

    private String linkedinUrl;
    private String githubUrl;

    private String gender;

    private LocalDate dateOfBirth;

    private List<String> skills;

    private String collegeName;
}