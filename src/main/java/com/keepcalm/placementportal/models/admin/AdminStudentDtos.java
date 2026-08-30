package com.keepcalm.placementportal.models.admin;
import com.keepcalm.placementportal.enums.*;
import jakarta.validation.constraints.*;
public final class AdminStudentDtos {
 private AdminStudentDtos(){}
 public record StudentDto(Long id,String username,String email,String firstName,String lastName,String rollNumber,String branch,Integer graduationYear,java.math.BigDecimal cgpa,int activeBacklogs,PlacementStatus placementStatus,int profileCompletion,long version){}
 public record PlacementUpdate(@NotNull PlacementStatus status){}
 public record VerificationUpdate(@NotNull DocumentVerificationStatus status,@Size(max=500)String note){}
}
