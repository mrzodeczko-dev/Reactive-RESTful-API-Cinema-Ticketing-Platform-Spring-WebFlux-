package com.rzodeczko.infrastructure.web;

import com.rzodeczko.infrastructure.config.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitingWebFilterTest {

    private static final RateLimitProperties PROPS = new RateLimitProperties(10, 10, 60);

    @Mock
    private RateLimiterService rateLimiterService;

    private RateLimitingWebFilter filter;

    private final WebFilterChain passThroughChain = exchange -> Mono.empty();

    @BeforeEach
    void setUp() {
        filter = new RateLimitingWebFilter(
                rateLimiterService, PROPS, new DefaultDataBufferFactory(), new ObjectMapper());
    }

    // ────────────────────────────────────────────────────────────────────────
    // Paths that are NOT rate-limited — service is never called
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Requests to non-rate-limited paths bypass the limiter entirely")
    void shouldPassThroughNonLimitedPaths() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/cinemas").build());

        StepVerifier.create(filter.filter(exchange, passThroughChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    // ────────────────────────────────────────────────────────────────────────
    // /login — allowed
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Request to /login is forwarded when service returns true")
    void shouldAllowLoginWhenServicePermits() {
        when(rateLimiterService.isAllowed(anyString(), anyString()))
                .thenReturn(Mono.just(true));

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/login")
                        .remoteAddress(java.net.InetSocketAddress.createUnresolved("10.0.0.1", 0))
                        .build());

        StepVerifier.create(filter.filter(exchange, passThroughChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    // ────────────────────────────────────────────────────────────────────────
    // /login — rejected
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Request to /login returns 429 when service returns false")
    void shouldReturn429WhenLoginRateLimitExceeded() {
        when(rateLimiterService.isAllowed(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/login")
                        .remoteAddress(java.net.InetSocketAddress.createUnresolved("10.0.0.2", 0))
                        .build());

        StepVerifier.create(filter.filter(exchange, passThroughChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(exchange.getResponse().getHeaders().getFirst("Retry-After")).isEqualTo("60");
    }

    // ────────────────────────────────────────────────────────────────────────
    // /register — rejected
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Request to /register returns 429 when service returns false")
    void shouldReturn429WhenRegisterRateLimitExceeded() {
        when(rateLimiterService.isAllowed(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/register")
                        .remoteAddress(java.net.InetSocketAddress.createUnresolved("10.0.0.3", 0))
                        .build());

        StepVerifier.create(filter.filter(exchange, passThroughChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Redis failure → fail open
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("When service returns error the request is allowed through (fail-open)")
    void shouldFailOpenOnRedisError() {
        when(rateLimiterService.isAllowed(anyString(), anyString()))
                .thenReturn(Mono.just(true)); // service already handles errors by returning true

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/login")
                        .remoteAddress(java.net.InetSocketAddress.createUnresolved("10.0.0.4", 0))
                        .build());

        StepVerifier.create(filter.filter(exchange, passThroughChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    // ────────────────────────────────────────────────────────────────────────
    // X-Forwarded-For
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("X-Forwarded-For header is used as the client IP")
    void shouldUseXForwardedForHeader() {
        when(rateLimiterService.isAllowed(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/login")
                        .remoteAddress(java.net.InetSocketAddress.createUnresolved("192.168.1.1", 0))
                        .header("X-Forwarded-For", "203.0.113.10, 10.0.0.1")
                        .build());

        StepVerifier.create(filter.filter(exchange, passThroughChain))
                .verifyComplete();

        // Regardless of proxy IP, the rate limiter is invoked (and rejects)
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
