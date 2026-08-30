package com.keepcalm.placementportal.models.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record CurrentUserDto(Long id, Long institutionId, String institutionCode, String username,
                             String email, String role, boolean emailVerified) {
    public record Update(@Size(min = 3, max = 100) String username, @Email String email) {}
}
