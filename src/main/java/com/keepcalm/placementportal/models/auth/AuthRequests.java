package com.keepcalm.placementportal.models.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthRequests {
    private AuthRequests() {}

    public record ForgotPassword(@NotBlank @Email String email, String institutionCode) {}
    public record ResetPassword(@NotBlank String token, @NotBlank @Size(min = 8, max = 72) String newPassword) {}
    public record VerifyEmail(@NotBlank String token) {}
}
