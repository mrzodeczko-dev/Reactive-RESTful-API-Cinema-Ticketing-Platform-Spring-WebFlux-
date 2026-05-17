package com.rzodeczko.infrastructure.web;

import com.rzodeczko.infrastructure.config.RateLimitProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Reactive, Redis-backed rate limiter using a fixed-window counter.
 *
 * <p>A single Lua script executes atomically on the Redis server:
 * <ol>
 *   <li>Increment the counter for {@code rate_limit:{path}:{ip}}</li>
 *   <li>On first increment, set a TTL equal to the window length so the
 *       key expires automatically at the end of the window.</li>
 *   <li>Return 1 (allowed) or 0 (rejected).</li>
 * </ol>
 *
 * <p>Because the logic runs as a single Lua transaction on Redis, it is safe
 * across any number of application instances.
 */
@Service
@Slf4j
public class RateLimiterService {

    /**
     * Lua script:
     * KEYS[1] = rate-limit key  (e.g. "rate_limit:/login:10.0.0.1")
     * ARGV[1] = max requests per window
     * ARGV[2] = window length in seconds
     *
     * Returns 1 if the request is allowed, 0 if it should be rejected.
     */
    private static final String LUA_SCRIPT = """
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))
            end
            if current > tonumber(ARGV[1]) then
                return 0
            else
                return 1
            end
            """;

    private static final RedisScript<Long> SCRIPT =
            RedisScript.of(LUA_SCRIPT, Long.class);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final RateLimitProperties props;

    public RateLimiterService(ReactiveStringRedisTemplate redisTemplate,
                              RateLimitProperties props) {
        this.redisTemplate = redisTemplate;
        this.props = props;
    }

    /**
     * Returns {@code true} when the request is within the allowed rate,
     * {@code false} when it should be rejected with 429.
     *
     * @param path     request path (included in the key for per-path isolation)
     * @param clientIp resolved client IP address
     */
    public Mono<Boolean> isAllowed(String path, String clientIp) {
        String key = "rate_limit:%s:%s".formatted(path, clientIp);
        return redisTemplate.execute(
                        SCRIPT,
                        List.of(key),
                        List.of(
                                String.valueOf(props.capacity()),
                                String.valueOf(props.refillPeriodSeconds())))
                .next()
                .map(result -> result == 1L)
                .onErrorResume(ex -> {
                    // If Redis is unreachable, fail open (allow the request)
                    // to avoid a Redis outage blocking all logins.
                    log.error("Redis rate-limiter error for key '{}', failing open: {}", key, ex.getMessage());
                    return Mono.just(true);
                });
    }
}
