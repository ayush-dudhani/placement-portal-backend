package com.keepcalm.placementportal.service;

import com.keepcalm.placementportal.entity.User;
import com.keepcalm.placementportal.repository.UserRepository;
import com.keepcalm.placementportal.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public User getCurrentUser(String authHeader) {

        String token = authHeader.substring(7);

        String username = jwtUtil.extractUsername(token);

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
}