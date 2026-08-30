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

import com.keepcalm.placementportal.exception.DomainException;
import com.keepcalm.placementportal.service.auth.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

class RateLimitServiceTest {
    @Test
    void inMemoryFallbackEnforcesTheSameLimitContract() {
        RateLimitService limiter = new RateLimitService(mock(StringRedisTemplate.class));
        ReflectionTestUtils.setField(limiter, "redisEnabled", false);

        assertDoesNotThrow(() -> limiter.check("login:client-a", 2, Duration.ofMinutes(1)));
        assertDoesNotThrow(() -> limiter.check("login:client-a", 2, Duration.ofMinutes(1)));
        assertThrows(DomainException.class, () -> limiter.check("login:client-a", 2, Duration.ofMinutes(1)));
        assertDoesNotThrow(() -> limiter.check("login:client-b", 2, Duration.ofMinutes(1)));
    }

    @Test
    void redisCounterRejectsRequestsAboveTheLimit() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        doReturn(3L).when(redis).execute(any(), anyList(), any(Object[].class));
        RateLimitService limiter = new RateLimitService(redis);
        ReflectionTestUtils.setField(limiter, "redisEnabled", true);

        assertThrows(DomainException.class,
                () -> limiter.check("login:student@example.com", 2, Duration.ofMinutes(1)));
    }
}
