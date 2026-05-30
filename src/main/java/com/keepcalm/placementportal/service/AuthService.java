package com.keepcalm.placementportal.service;

import com.keepcalm.placementportal.entity.User;
import com.keepcalm.placementportal.enums.Role;
import com.keepcalm.placementportal.models.auth.ChangePasswordRequest;
import com.keepcalm.placementportal.models.auth.LoginRequest;
import com.keepcalm.placementportal.models.auth.LoginResponse;
import com.keepcalm.placementportal.models.auth.SignupRequest;
import com.keepcalm.placementportal.repository.UserRepository;
import com.keepcalm.placementportal.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public LoginResponse login(LoginRequest request) {
        String identifier = request.getUsername();
        if (identifier == null) {
            throw new RuntimeException("Invalid credentials");
        }

        // normalize and detect whether identifier is an email
        identifier = identifier.trim();
        boolean usedEmail = identifier.contains("@");
        if (usedEmail) {
            identifier = identifier.toLowerCase();
        }

        // lookup by the appropriate field
        User user = usedEmail
                ? userRepository.findByEmail(identifier)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"))
                : userRepository.findByUsername(identifier)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash());

        if (!passwordMatches) {
            throw new RuntimeException("Invalid credentials");
        }

        // generate token with subject as username include username/email/role claims
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );

        String jti = jwtUtil.extractJti(token);
        String redisKey = "session:" + user.getId() + ":" + jti;

        // store token (or metadata) keyed by userId:jti with TTL matching token expiration
        redisTemplate.opsForValue().set(
                redisKey,
                token,
                Duration.ofMillis(jwtUtil.getExpirationMillis())
        );

        // Build response exposing only the identifier that was supplied for login
        LoginResponse.LoginResponseBuilder resp = LoginResponse.builder()
                .token(token)
                .role(user.getRole().name());

        if (usedEmail) {
            resp.email(user.getEmail());
        } else {
            resp.username(user.getUsername());
        }

        return resp.build();
    }

    public void signup(SignupRequest request) {

        boolean usernameTaken = userRepository.existsByUsername(request.getUsername());
        boolean emailTaken = userRepository.existsByEmail(request.getEmail());

        if (usernameTaken || emailTaken) {
            throw new RuntimeException("Username or email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // normalize email to lowercase
        user.setEmail(request.getEmail().trim().toLowerCase());

        // default role is STUDENT for all new signups
        user.setRole(Role.STUDENT);

        // important part: store hashed password in passwordHash
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
    }

    public void changePassword(
            String authHeader,
            ChangePasswordRequest request) {

        String token = authHeader.substring(7);

        // token subject is username (subject contains username)
        String usernameFromToken = jwtUtil.extractUsername(token);
        if (usernameFromToken == null) {
            throw new RuntimeException("Invalid token subject");
        }

        User user = userRepository.findByUsername(usernameFromToken)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean validOldPassword = passwordEncoder.matches(
                request.getOldPassword(),
                user.getPasswordHash());

        if (!validOldPassword) {
            throw new RuntimeException(
                    "Incorrect old password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        // Invalidate all sessions for this user (e.g., after password change)
        try {
            Set<String> keys = redisTemplate.keys("session:" + user.getId() + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ex) {
            // ignore if keys operation is unsupported
        }
    }
}