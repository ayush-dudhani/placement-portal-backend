package com.keepcalm.placementportal.service;

import com.keepcalm.placementportal.exception.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {
    private static final DefaultRedisScript<Long> INCREMENT = new DefaultRedisScript<>("""
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then
              redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            return count
            """, Long.class);

    private final StringRedisTemplate redis;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Value("${app.redis.enabled:true}")
    private boolean redisEnabled;

    public RateLimitService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void check(String key, int limit, Duration duration) {
        if (redisEnabled) {
            Long count = redis.execute(INCREMENT, List.of("placement:rate-limit:" + digest(key)),
                    Long.toString(duration.toMillis()));
            if (count == null) {
                throw new IllegalStateException("Redis did not return a rate-limit counter");
            }
            if (count > limit) {
                rateLimited();
            }
            return;
        }

        Instant now = Instant.now();
        Window window = windows.compute(key, (ignored, current) ->
                current == null || current.expiresAt.isBefore(now) ? new Window(now.plus(duration)) : current);
        if (window.count.incrementAndGet() > limit) {
            rateLimited();
        }
    }

    private void rateLimited() {
        throw new DomainException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Too many requests; try again later");
    }

    private String digest(String key) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final class Window {
        private final Instant expiresAt;
        private final AtomicInteger count = new AtomicInteger();
        private Window(Instant expiresAt) { this.expiresAt = expiresAt; }
    }
}
