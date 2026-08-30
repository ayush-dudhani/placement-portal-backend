package com.keepcalm.placementportal.security;

import com.keepcalm.placementportal.entity.User;
import com.keepcalm.placementportal.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false")
public class InMemoryAccessSessionStore implements AccessSessionStore {
    private final JwtUtil jwtUtil;
    private final Map<String, Instant> sessions = new ConcurrentHashMap<>();

    @Override
    public void register(User user, String accessToken) {
        sessions.put(key(user.getInstitution().getId(), user.getId(), jwtUtil.extractJti(accessToken)),
                jwtUtil.extractExpiration(accessToken));
    }

    @Override
    public boolean isActive(long institutionId, long userId, String jti) {
        Instant expiry = sessions.get(key(institutionId, userId, jti));
        if (expiry == null || !expiry.isAfter(Instant.now())) {
            sessions.remove(key(institutionId, userId, jti));
            return false;
        }
        return true;
    }

    @Override
    public void revoke(String accessToken) {
        Long institutionId = jwtUtil.extractInstitutionId(accessToken);
        Long userId = jwtUtil.extractUserId(accessToken);
        if (institutionId != null && userId != null) {
            sessions.remove(key(institutionId, userId, jwtUtil.extractJti(accessToken)));
        }
    }

    @Override
    public void revokeAll(long institutionId, long userId) {
        String prefix = institutionId + ":" + userId + ':';
        sessions.keySet().removeIf(key -> key.startsWith(prefix));
    }

    private String key(long institutionId, long userId, String jti) {
        return institutionId + ":" + userId + ':' + jti;
    }
}
