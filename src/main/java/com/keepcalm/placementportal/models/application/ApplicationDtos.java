package com.keepcalm.placementportal.models.application;
import com.keepcalm.placementportal.enums.ApplicationStatus;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.*;
public final class ApplicationDtos {
 private ApplicationDtos(){}
 public record ApplicationDto(Long id,Long driveId,String driveTitle,Long roleId,String roleTitle,ApplicationStatus status,
   boolean eligibilityPassed,Map<String,Object> eligibilitySnapshot,Instant appliedAt,long version){}
 public record HistoryDto(Long id,ApplicationStatus fromStatus,ApplicationStatus toStatus,String changedBy,String reason,Instant createdAt){}
 public record Withdraw(@Size(max=500)String reason){}
 public record StatusUpdate(ApplicationStatus status,@Size(max=500)String reason){}
 public record BulkStatusUpdate(@jakarta.validation.constraints.NotEmpty List<Long> applicationIds,ApplicationStatus status,@Size(max=500)String reason){}
 public record BulkIds(@jakarta.validation.constraints.NotEmpty List<Long> applicationIds){}
}
