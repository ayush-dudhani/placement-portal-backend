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
