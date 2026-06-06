package com.keepcalm.placementportal.controller;

import com.keepcalm.placementportal.models.auth.ChangePasswordRequest;
import com.keepcalm.placementportal.models.auth.LoginRequest;
import com.keepcalm.placementportal.models.auth.LoginResponse;
import com.keepcalm.placementportal.models.auth.SignupRequest;
import com.keepcalm.placementportal.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(
            @Valid @RequestBody SignupRequest request) {

        authService.signup(request);

        return ResponseEntity.ok(
                "User created successfully"
        );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChangePasswordRequest request) {

        authService.changePassword(
                authHeader,
                request
        );

        return ResponseEntity.ok(
                "Password changed successfully"
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);

        authService.logout(token);

        return ResponseEntity.ok("Logged out successfully");
    }
}