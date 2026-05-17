package com.keepcalm.placementportal.service;

import com.keepcalm.placementportal.entity.User;
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

        User user = userRepository
                .findByCollegeNameAndUsername(
                        request.getCollegeName(),
                        request.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("Invalid credentials"));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword());

        if (!passwordMatches) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole()
        );

        String redisKey = "session:" + user.getId();

        redisTemplate.opsForValue().set(
                redisKey,
                token,
                Duration.ofHours(8)
        );

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .collegeName(user.getCollegeName())
                .build();
    }

    public void signup(SignupRequest request) {

        boolean userExists =
                userRepository
                        .existsByCollegeNameAndUsername(
                                request.getCollegeName(),
                                request.getUsername());

        if (userExists) {
            throw new RuntimeException(
                    "Username already exists"
            );
        }

        User user = new User();

        user.setCollegeName(
                request.getCollegeName());

        user.setUsername(
                request.getUsername());

        user.setRole(
                request.getRole());

        // important part
        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        userRepository.save(user);
    }

    public void changePassword(
            String authHeader,
            ChangePasswordRequest request) {

        String token = authHeader.substring(7);

        String username =
                jwtUtil.extractUsername(token);

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"));

        boolean validOldPassword =
                passwordEncoder.matches(
                        request.getOldPassword(),
                        user.getPassword());

        if (!validOldPassword) {
            throw new RuntimeException(
                    "Incorrect old password");
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()));

        userRepository.save(user);

        // Invalidate current session/token
        redisTemplate.delete(
                "session:" + user.getId()
        );
    }
}