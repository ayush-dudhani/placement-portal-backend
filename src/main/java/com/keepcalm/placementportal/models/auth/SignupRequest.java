package com.keepcalm.placementportal.models.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "Email is required field")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Username is required field")
    private String username;

    @NotBlank(message = "Password is required field")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
}