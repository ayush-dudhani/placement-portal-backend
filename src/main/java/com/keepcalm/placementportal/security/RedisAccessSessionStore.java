package com.keepcalm.placementportal.security;

import com.keepcalm.placementportal.entity.User;
import com.keepcalm.placementportal.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisAccessSessionStore implements AccessSessionStore {
    private static final String PREFIX = "placement:auth";
    private final StringRedisTemplate redis;
    private final JwtUtil jwtUtil;

    @Override
    public void register(User user, String accessToken) {
        String jti = jwtUtil.extractJti(accessToken);
        Duration ttl = jwtUtil.remainingLifetime(accessToken);
        if (jti == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("Cannot register an expired access token");
        }
        long institutionId = user.getInstitution().getId();
        long userId = user.getId();
        redis.opsForValue().set(sessionKey(institutionId, userId, jti), "active", ttl);
        redis.opsForSet().add(userSessionsKey(institutionId, userId), jti);
        redis.expire(userSessionsKey(institutionId, userId), ttl.plusSeconds(30));
    }

    @Override
    public boolean isActive(long institutionId, long userId, String jti) {
        return jti != null && Boolean.TRUE.equals(redis.hasKey(sessionKey(institutionId, userId, jti)));
    }

    @Override
    public void revoke(String accessToken) {
        Long institutionId = jwtUtil.extractInstitutionId(accessToken);
        Long userId = jwtUtil.extractUserId(accessToken);
        String jti = jwtUtil.extractJti(accessToken);
        if (institutionId == null || userId == null || jti == null) {
            return;
        }
        redis.delete(sessionKey(institutionId, userId, jti));
        redis.opsForSet().remove(userSessionsKey(institutionId, userId), jti);
    }

    @Override
    public void revokeAll(long institutionId, long userId) {
        String indexKey = userSessionsKey(institutionId, userId);
        Set<String> sessionIds = redis.opsForSet().members(indexKey);
        if (sessionIds != null && !sessionIds.isEmpty()) {
            redis.delete(sessionIds.stream().map(jti -> sessionKey(institutionId, userId, jti)).toList());
        }
        redis.delete(indexKey);
    }

    private String sessionKey(long institutionId, long userId, String jti) {
        return PREFIX + ":session:" + institutionId + ':' + userId + ':' + jti;
    }

    private String userSessionsKey(long institutionId, long userId) {
        return PREFIX + ":user-sessions:" + institutionId + ':' + userId;
    }
}
