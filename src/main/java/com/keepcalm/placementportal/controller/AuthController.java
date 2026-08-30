package com.keepcalm.placementportal.controller;

import com.keepcalm.placementportal.models.auth.*;
import com.keepcalm.placementportal.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_COOKIE = "refresh_token";
    private final AuthService authService;
    @Value("${auth.refresh-cookie-secure}") private boolean secureCookie;
    @Value("${jwt.refresh-expiration}") private long refreshExpiration;

    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<LoginResponse.UserSummary> signup(@Valid @RequestBody SignupRequest request, HttpServletRequest servletRequest) {
        LoginResponse.UserSummary user = authService.signup(request, servletRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return withRefreshCookie(authService.login(request, servletRequest.getRemoteAddr()));
    }

    @PostMapping("/api/v1/auth/refresh")
    public ResponseEntity<LoginResponse> refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        return withRefreshCookie(authService.refresh(refreshToken));
    }

    @PostMapping("/api/v1/auth/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        authService.logout(refreshToken, bearerToken(authorization));
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, clearCookie().toString()).build();
    }

    @PostMapping("/api/v1/auth/forgot-password")
    public ResponseEntity<Map<String, String>> forgot(@Valid @RequestBody AuthRequests.ForgotPassword request, HttpServletRequest servletRequest) {
        authService.forgotPassword(request, servletRequest.getRemoteAddr());
        return ResponseEntity.accepted().body(Map.of("message", "If the account exists, password reset instructions will be sent"));
    }

    @PostMapping("/api/v1/auth/reset-password")
    public ResponseEntity<Void> reset(@Valid @RequestBody AuthRequests.ResetPassword request, HttpServletRequest servletRequest) {
        authService.resetPassword(request, servletRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/auth/verify-email")
    public ResponseEntity<Void> verify(@Valid @RequestBody AuthRequests.VerifyEmail request, HttpServletRequest servletRequest) {
        authService.verifyEmail(request, servletRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/auth/session")
    @PreAuthorize("isAuthenticated()")
    public LoginResponse.UserSummary session() { return authService.session(); }

    @PutMapping("/api/v1/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<LoginResponse> withRefreshCookie(AuthService.LoginResult result) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, result.refreshToken())
                .httpOnly(true).secure(secureCookie).sameSite("Strict").path("/api/v1/auth")
                .maxAge(Duration.ofMillis(refreshExpiration)).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(result.response());
    }

    private ResponseCookie clearCookie() {
        return ResponseCookie.from(REFRESH_COOKIE, "").httpOnly(true).secure(secureCookie)
                .sameSite("Strict").path("/api/v1/auth").maxAge(Duration.ZERO).build();
    }

    private String bearerToken(String authorization) {
        return authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
    }
}
