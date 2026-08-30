package com.keepcalm.placementportal.models.student;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AcademicRecordDto(Long id,
        @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal cgpa,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal tenthPercentage,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal twelfthPercentage,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal diplomaPercentage,
        @Min(0) int activeBacklogs,
        @NotBlank @Size(max = 100) String branch,
        @Min(2000) @Max(2200) int graduationYear) {
}
