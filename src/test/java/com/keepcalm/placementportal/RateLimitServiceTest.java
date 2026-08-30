package com.keepcalm.placementportal;

import com.keepcalm.placementportal.exception.DomainException;
import com.keepcalm.placementportal.service.RateLimitService;
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
