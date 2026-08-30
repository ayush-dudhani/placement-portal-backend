package com.keepcalm.placementportal.service.auth;
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

import com.keepcalm.placementportal.entity.auth.*;
import com.keepcalm.placementportal.entity.profile.*;
import com.keepcalm.placementportal.entity.student.*;
import com.keepcalm.placementportal.entity.company.*;
import com.keepcalm.placementportal.entity.drive.*;
import com.keepcalm.placementportal.entity.application.*;
import com.keepcalm.placementportal.entity.selection.*;
import com.keepcalm.placementportal.entity.offer.*;
import com.keepcalm.placementportal.entity.communication.*;
import com.keepcalm.placementportal.entity.audit.*;
import com.keepcalm.placementportal.enums.PlacementStatus;
import com.keepcalm.placementportal.enums.Role;
import com.keepcalm.placementportal.exception.DomainException;
import com.keepcalm.placementportal.models.auth.*;
import com.keepcalm.placementportal.repository.auth.*;
import com.keepcalm.placementportal.repository.profile.*;
import com.keepcalm.placementportal.repository.student.*;
import com.keepcalm.placementportal.repository.company.*;
import com.keepcalm.placementportal.repository.drive.*;
import com.keepcalm.placementportal.repository.application.*;
import com.keepcalm.placementportal.repository.selection.*;
import com.keepcalm.placementportal.repository.offer.*;
import com.keepcalm.placementportal.repository.communication.*;
import com.keepcalm.placementportal.repository.audit.*;
import com.keepcalm.placementportal.security.AccessSessionStore;
import com.keepcalm.placementportal.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final InstitutionRepository institutionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository accountTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CurrentUserService currentUserService;
    private final RateLimitService rateLimitService;
    private final AccessSessionStore accessSessions;
    private final SecureRandom random = new SecureRandom();

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMillis;

    @Transactional
    public LoginResult login(LoginRequest request, String clientKey) {
        rateLimitService.check("login:" + clientKey + ":" + request.getUsername().toLowerCase(), 10, Duration.ofMinutes(15));
        User user = findUser(request.getUsername(), request.getInstitutionCode());
        if (!Boolean.TRUE.equals(user.getIsActive()) || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        return issueSession(user);
    }

    @Transactional
    public LoginResponse.UserSummary signup(SignupRequest request, String clientKey) {
        rateLimitService.check("register:" + clientKey, 5, Duration.ofHours(1));
        Institution institution = institution(request.getInstitutionCode());
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByInstitutionIdAndUsernameIgnoreCase(institution.getId(), username)
                || userRepository.existsByInstitutionIdAndEmailIgnoreCase(institution.getId(), email)) {
            throw DomainException.conflict("ACCOUNT_EXISTS", "Username or email already exists in this institution");
        }
        User user = new User();
        user.setInstitution(institution);
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(Role.STUDENT);
        user.setIsActive(true);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        studentRepository.save(Student.builder().user(user).institution(institution).placementStatus(PlacementStatus.NOT_PLACED).build());
        createAccountToken(user, PasswordResetToken.Purpose.EMAIL_VERIFICATION, Duration.ofHours(24));
        return summary(user);
    }

    @Transactional
    public LoginResult refresh(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is missing");
        }
        RefreshToken current = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new DomainException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is invalid"));
        if (!current.usableAt(Instant.now()) || !Boolean.TRUE.equals(current.getUser().getIsActive())) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is expired or revoked");
        }
        String replacementRaw = randomToken();
        current.setRevokedAt(Instant.now());
        current.setReplacedByHash(hash(replacementRaw));
        RefreshToken replacement = new RefreshToken();
        replacement.setUser(current.getUser());
        replacement.setTokenHash(hash(replacementRaw));
        replacement.setExpiresAt(Instant.now().plusMillis(refreshExpirationMillis));
        refreshTokenRepository.save(replacement);
        return new LoginResult(response(current.getUser()), replacementRaw);
    }

    @Transactional
    public void logout(String rawRefreshToken, String rawAccessToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenRepository.findByTokenHash(hash(rawRefreshToken)).ifPresent(token -> token.setRevokedAt(Instant.now()));
        }
        if (rawAccessToken != null && !rawAccessToken.isBlank()) {
            accessSessions.revoke(rawAccessToken);
        }
    }

    public void logout(String rawRefreshToken) { logout(rawRefreshToken, null); }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUserService.getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, "INCORRECT_PASSWORD", "Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        revokeAll(user);
    }

    @Transactional
    public void forgotPassword(AuthRequests.ForgotPassword request, String clientKey) {
        rateLimitService.check("forgot:" + clientKey, 5, Duration.ofHours(1));
        Institution institution = institution(request.institutionCode());
        userRepository.findByInstitutionIdAndEmailIgnoreCase(institution.getId(), request.email().trim())
                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                .ifPresent(user -> createAccountToken(user, PasswordResetToken.Purpose.PASSWORD_RESET, Duration.ofMinutes(30)));
    }

    @Transactional
    public void resetPassword(AuthRequests.ResetPassword request, String clientKey) {
        rateLimitService.check("reset:" + clientKey, 10, Duration.ofHours(1));
        PasswordResetToken token = consumeAccountToken(request.token(), PasswordResetToken.Purpose.PASSWORD_RESET);
        token.getUser().setPasswordHash(passwordEncoder.encode(request.newPassword()));
        revokeAll(token.getUser());
    }

    @Transactional
    public void verifyEmail(AuthRequests.VerifyEmail request, String clientKey) {
        rateLimitService.check("verify:" + clientKey, 10, Duration.ofHours(1));
        consumeAccountToken(request.token(), PasswordResetToken.Purpose.EMAIL_VERIFICATION).getUser().setEmailVerified(true);
    }

    @Transactional(readOnly = true)
    public LoginResponse.UserSummary session() { return summary(currentUserService.getCurrentUser()); }

    private LoginResult issueSession(User user) {
        String rawRefresh = randomToken();
        RefreshToken refresh = new RefreshToken();
        refresh.setUser(user);
        refresh.setTokenHash(hash(rawRefresh));
        refresh.setExpiresAt(Instant.now().plusMillis(refreshExpirationMillis));
        refreshTokenRepository.save(refresh);
        return new LoginResult(response(user), rawRefresh);
    }

    private LoginResponse response(User user) {
        String accessToken = jwtUtil.generateToken(user);
        accessSessions.register(user, accessToken);
        return LoginResponse.builder().accessToken(accessToken).tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMillis() / 1000).user(summary(user)).build();
    }

    private LoginResponse.UserSummary summary(User user) {
        return new LoginResponse.UserSummary(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }

    private User findUser(String identifier, String institutionCode) {
        Institution institution = institution(institutionCode);
        String normalized = identifier.trim();
        return (normalized.contains("@")
                ? userRepository.findByInstitutionIdAndEmailIgnoreCase(institution.getId(), normalized)
                : userRepository.findByInstitutionIdAndUsernameIgnoreCase(institution.getId(), normalized))
                .orElseThrow(this::invalidCredentials);
    }

    private Institution institution(String code) {
        String normalized = code == null || code.isBlank() ? "DEFAULT" : code.trim();
        return institutionRepository.findByCodeIgnoreCaseAndActiveTrue(normalized)
                .orElseThrow(() -> DomainException.notFound("Institution not found"));
    }

    private void createAccountToken(User user, PasswordResetToken.Purpose purpose, Duration duration) {
        String raw = randomToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setPurpose(purpose);
        token.setTokenHash(hash(raw));
        token.setExpiresAt(Instant.now().plus(duration));
        accountTokenRepository.save(token);
        // The raw token is intentionally not logged or returned; a mail adapter delivers it after commit.
    }

    private PasswordResetToken consumeAccountToken(String raw, PasswordResetToken.Purpose purpose) {
        PasswordResetToken token = accountTokenRepository.findByTokenHashAndPurpose(hash(raw), purpose)
                .orElseThrow(() -> new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_TOKEN", "Token is invalid"));
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(Instant.now())) {
            throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_TOKEN", "Token is expired or already used");
        }
        token.setUsedAt(Instant.now());
        return token;
    }

    private void revokeAll(User user) {
        refreshTokenRepository.findByUserIdAndRevokedAtIsNull(user.getId()).forEach(token -> token.setRevokedAt(Instant.now()));
        accessSessions.revokeAll(user.getInstitution().getId(), user.getId());
    }

    private DomainException invalidCredentials() {
        return new DomainException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials");
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public record LoginResult(LoginResponse response, String refreshToken) {}
}
