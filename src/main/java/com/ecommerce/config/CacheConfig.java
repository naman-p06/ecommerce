package com.ecommerce.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching    // This annotation activates @Cacheable, @CacheEvict, @CachePut
public class CacheConfig {

    @Value("${cache.ttl.products:600}")
    private long productsTtl;

    @Value("${cache.ttl.product-by-id:900}")
    private long productByIdTtl;

    @Value("${cache.ttl.categories:1800}")
    private long categoriesTtl;

    @Value("${cache.ttl.search:300}")
    private long searchTtl;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {

        // ── Serializer setup ────────────────────────────────────────────
        // CRITICAL: Use Jackson JSON serialization, NOT Java's default serialization.
        //
        // Java serialization stores class names + binary data — if you ever rename
        // a class, every cached entry becomes unreadable and causes ClassNotFound errors.
        // Jackson stores clean JSON — human-readable in Redis, version-tolerant.
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Activate default typing so Jackson knows the concrete type when deserializing
        // back from Redis (needed because cache stores Object, not a specific class)
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(mapper);

        // ── Default cache configuration ─────────────────────────────────
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                // Keys stored as plain UTF-8 strings — readable in Redis CLI
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                // Values stored as JSON — readable + version-tolerant
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                // Never cache a null value — prevents "null poisoning" where a missing
                // DB record gets cached as null and blocks real data from being cached
                .disableCachingNullValues();

        // ── Per-cache TTL overrides ─────────────────────────────────────
        // Products page list: 10 min — refreshed frequently, changes with add/delete
        // Product by id:      15 min — individual products change less often
        // Categories:         30 min — categories almost never change
        // Search results:      5 min — short TTL because search results change with stock
        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                "products",       defaultConfig.entryTtl(Duration.ofSeconds(productsTtl)),
                "product-by-id",  defaultConfig.entryTtl(Duration.ofSeconds(productByIdTtl)),
                "categories",     defaultConfig.entryTtl(Duration.ofSeconds(categoriesTtl)),
                "search",         defaultConfig.entryTtl(Duration.ofSeconds(searchTtl))
        );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}