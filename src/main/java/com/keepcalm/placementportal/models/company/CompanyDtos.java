package com.keepcalm.placementportal.models.company;
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
