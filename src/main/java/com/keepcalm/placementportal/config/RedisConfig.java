package com.keepcalm.placementportal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
public class RedisConfig {

    @Bean
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true", matchIfMissing = true)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        GenericJackson2JsonRedisSerializer json = cacheSerializer(objectMapper);
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(json);
        template.setHashValueSerializer(json);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true", matchIfMissing = true)
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisSerializationContext.SerializationPair<Object> values = RedisSerializationContext.SerializationPair
                .fromSerializer(cacheSerializer(objectMapper));
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofSeconds(60))
                .serializeValuesWith(values);
        Map<String, RedisCacheConfiguration> configurations = Map.of(
                "companies", defaults.entryTtl(Duration.ofMinutes(5)),
                "drives", defaults.entryTtl(Duration.ofMinutes(1)),
                "analytics", defaults.entryTtl(Duration.ofMinutes(1)),
                "announcements", defaults.entryTtl(Duration.ofMinutes(1)),
                "notification-unread", defaults.entryTtl(Duration.ofSeconds(30)));
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(configurations)
                .transactionAware()
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false")
    public CacheManager inMemoryCacheManager() {
        return new ConcurrentMapCacheManager("companies", "drives", "analytics", "announcements", "notification-unread");
    }

    GenericJackson2JsonRedisSerializer cacheSerializer(ObjectMapper objectMapper) {
        BasicPolymorphicTypeValidator types = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.keepcalm.placementportal")
                .allowIfSubType("java.util")
                .allowIfSubType("java.time")
                .allowIfSubType("java.math")
                .allowIfSubType(Pattern.compile("java\\.lang\\.(String|Long|Integer|Boolean|Double|Float|Short|Byte|Character)"))
                .build();
        ObjectMapper cacheMapper = objectMapper.copy();
        cacheMapper.activateDefaultTyping(types, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(cacheMapper);
    }
}
