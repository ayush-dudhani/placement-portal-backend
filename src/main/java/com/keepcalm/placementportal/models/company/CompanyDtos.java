package com.keepcalm.placementportal.models.company;
import com.keepcalm.placementportal.repository.audit.*;
import com.keepcalm.placementportal.repository.communication.*;
import com.keepcalm.placementportal.repository.offer.*;
import com.keepcalm.placementportal.repository.selection.*;
import com.keepcalm.placementportal.repository.application.*;
import com.keepcalm.placementportal.repository.drive.*;
import com.keepcalm.placementportal.repository.company.*;
import com.keepcalm.placementportal.repository.profile.*;
import com.keepcalm.placementportal.repository.student.*;
import com.keepcalm.placementportal.repository.auth.*;
import com.keepcalm.placementportal.service.storage.*;
import com.keepcalm.placementportal.service.audit.*;
import com.keepcalm.placementportal.service.event.*;
import com.keepcalm.placementportal.service.analytics.*;
import com.keepcalm.placementportal.service.communication.*;
import com.keepcalm.placementportal.service.offer.*;
import com.keepcalm.placementportal.service.selection.*;
import com.keepcalm.placementportal.service.application.*;
import com.keepcalm.placementportal.service.drive.*;
import com.keepcalm.placementportal.service.company.*;
import com.keepcalm.placementportal.service.student.*;
import com.keepcalm.placementportal.service.profile.*;
import com.keepcalm.placementportal.service.auth.*;
import com.keepcalm.placementportal.entity.audit.*;
import com.keepcalm.placementportal.entity.communication.*;
import com.keepcalm.placementportal.entity.offer.*;
import com.keepcalm.placementportal.entity.selection.*;
import com.keepcalm.placementportal.entity.application.*;
import com.keepcalm.placementportal.entity.drive.*;
import com.keepcalm.placementportal.entity.company.*;
import com.keepcalm.placementportal.entity.profile.*;
import com.keepcalm.placementportal.entity.student.*;
import com.keepcalm.placementportal.entity.auth.*;
import com.keepcalm.placementportal.controller.event.*;
import com.keepcalm.placementportal.controller.analytics.*;
import com.keepcalm.placementportal.controller.communication.*;
import com.keepcalm.placementportal.controller.offer.*;
import com.keepcalm.placementportal.controller.selection.*;
import com.keepcalm.placementportal.controller.application.*;
import com.keepcalm.placementportal.controller.drive.*;
import com.keepcalm.placementportal.controller.company.*;
import com.keepcalm.placementportal.controller.student.*;
import com.keepcalm.placementportal.controller.profile.*;
import com.keepcalm.placementportal.controller.auth.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
public final class CompanyDtos {
 private CompanyDtos(){}
 public record CompanyDto(Long id,String name,String industry,String description,String websiteUrl,String logoUrl,long version){}
 public record Upsert(@NotBlank @Size(max=255)String name,@Size(max=100)String industry,@Size(max=10000)String description,@Size(max=500)String websiteUrl,@Size(max=500)String logoUrl){}
 public record ContactDto(Long id,String name,String email,String phone,String designation,boolean primary,long version){}
 public record ContactUpsert(@NotBlank @Size(max=150)String name,@NotBlank @Email String email,@Size(max=30)String phone,@Size(max=100)String designation,boolean primary){}
 public record FaqDto(Long id,String question,String answer,int displayOrder){}
 public record ReplyDto(Long id,String authorRole,String body,Instant createdAt){}
 public record QuestionDto(Long id,String question,String askedBy,boolean anonymous,boolean closed,Instant createdAt,List<ReplyDto> replies){}
 public record Ask(@NotBlank @Size(max=5000)String question,boolean anonymous){}
 public record Reply(@NotBlank @Size(max=5000)String body){}
}
