package com.keepcalm.placementportal.models.student;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.keepcalm.placementportal.enums.PlacementStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentProfileResponse {

    private String collegeName;

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

    private PlacementStatus placementStatus;

    private Integer profileCompletion;
}