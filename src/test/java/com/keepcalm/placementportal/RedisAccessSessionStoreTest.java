package com.keepcalm.placementportal;
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

import com.keepcalm.placementportal.entity.auth.Institution;
import com.keepcalm.placementportal.entity.auth.User;
import com.keepcalm.placementportal.security.RedisAccessSessionStore;
import com.keepcalm.placementportal.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RedisAccessSessionStoreTest {
    @SuppressWarnings("unchecked")
    @Test
    void registersValidatesAndRevokesAccessSessionsWithoutKeyScans() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        SetOperations<String, String> sets = mock(SetOperations.class);
        JwtUtil jwt = mock(JwtUtil.class);
        when(redis.opsForValue()).thenReturn(values);
        when(redis.opsForSet()).thenReturn(sets);
        when(jwt.extractJti("jwt")).thenReturn("jti-1");
        when(jwt.extractInstitutionId("jwt")).thenReturn(3L);
        when(jwt.extractUserId("jwt")).thenReturn(7L);
        when(jwt.remainingLifetime("jwt")).thenReturn(Duration.ofMinutes(15));
        when(redis.hasKey("placement:auth:session:3:7:jti-1")).thenReturn(true);

        Institution institution = new Institution();
        institution.setId(3L);
        User user = new User();
        user.setId(7L);
        user.setInstitution(institution);
        RedisAccessSessionStore sessions = new RedisAccessSessionStore(redis, jwt);

        sessions.register(user, "jwt");
        assertTrue(sessions.isActive(3L, 7L, "jti-1"));
        sessions.revoke("jwt");

        verify(values).set("placement:auth:session:3:7:jti-1", "active", Duration.ofMinutes(15));
        verify(sets).add("placement:auth:user-sessions:3:7", "jti-1");
        verify(redis).delete("placement:auth:session:3:7:jti-1");
        verify(sets).remove("placement:auth:user-sessions:3:7", "jti-1");

        when(sets.members("placement:auth:user-sessions:3:7")).thenReturn(Set.of("jti-2", "jti-3"));
        sessions.revokeAll(3L, 7L);
        verify(redis).delete(org.mockito.ArgumentMatchers.<Collection<String>>argThat(keys -> keys.size() == 2
                && keys.containsAll(List.of("placement:auth:session:3:7:jti-2", "placement:auth:session:3:7:jti-3"))));
        verify(redis).delete("placement:auth:user-sessions:3:7");
    }
}
