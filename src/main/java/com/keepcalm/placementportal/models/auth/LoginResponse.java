package com.keepcalm.placementportal.models.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private UserSummary user;

    public record UserSummary(Long id, String username, String email, String role) {}
}
