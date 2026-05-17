package com.rzodeczko.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the rate limiter applied to public auth endpoints
 * (/login, /register).
 *
 * <pre>
 * rate-limit:
 *   capacity: 10          # max burst allowed
 *   refill-tokens: 10     # tokens refilled per period
 *   refill-period-seconds: 60  # period length in seconds
 * </pre>
 */
@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        long capacity,
        long refillTokens,
        long refillPeriodSeconds
) {
}
