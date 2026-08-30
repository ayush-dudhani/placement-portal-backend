package com.keepcalm.placementportal.security;
import com.keepcalm.placementportal.repository.audit.*;
import com.keepcalm.placementportal.repository.communication.*;
import com.keepcalm.placementportal.repository.offer.*;
import com.keepcalm.placementportal.repository.selection.*;
import com.keepcalm.placementportal.repository.application.*;
import com.keepcalm.placementportal.repository.drive.*;
import com.keepcalm.placementportal.repository.company.*;
import com.keepcalm.placementportal.repository.profile.*;
import com.keepcalm.placementportal.repository.student.*;
import com.keepcalm.placementportal.repository.auth.*;
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
import com.keepcalm.placementportal.entity.audit.*;
import com.keepcalm.placementportal.entity.communication.*;
import com.keepcalm.placementportal.entity.offer.*;
import com.keepcalm.placementportal.entity.selection.*;
import com.keepcalm.placementportal.entity.application.*;
import com.keepcalm.placementportal.entity.drive.*;
import com.keepcalm.placementportal.entity.company.*;
import com.keepcalm.placementportal.entity.profile.*;
import com.keepcalm.placementportal.entity.student.*;
import com.keepcalm.placementportal.entity.auth.*;
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

import com.keepcalm.placementportal.entity.auth.User;
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
