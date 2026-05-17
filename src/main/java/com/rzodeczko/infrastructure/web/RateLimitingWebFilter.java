package com.rzodeczko.infrastructure.web;

import com.rzodeczko.application.dto.ErrorMessageDto;
import com.rzodeczko.application.dto.ResponseErrorDto;
import com.rzodeczko.infrastructure.config.RateLimitProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * Per-IP rate limiter for public auth endpoints (/login, /register).
 *
 * <p>Delegates the actual counting to {@link RateLimiterService}, which uses
 * Redis + a Lua script to ensure atomic, multi-instance-safe accounting.
 *
 * <p>On Redis failure the limiter fails open (requests are allowed through).
 * Runs just after {@link RequestIdWebFilter} (order = HIGHEST_PRECEDENCE + 1).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
public class RateLimitingWebFilter implements WebFilter {

    private static final Set<String> RATE_LIMITED_PATHS = Set.of("/login", "/register");

    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties props;
    private final DataBufferFactory dataBufferFactory;
    private final ObjectMapper objectMapper;

    public RateLimitingWebFilter(RateLimiterService rateLimiterService,
                                 RateLimitProperties props,
                                 DataBufferFactory dataBufferFactory,
                                 ObjectMapper objectMapper) {
        this.rateLimiterService = rateLimiterService;
        this.props = props;
        this.dataBufferFactory = dataBufferFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (!RATE_LIMITED_PATHS.contains(path)) {
            return chain.filter(exchange);
        }

        String clientIp = resolveClientIp(exchange);

        return rateLimiterService.isAllowed(path, clientIp)
                .flatMap(allowed -> {
                    if (allowed) {
                        return chain.filter(exchange);
                    }
                    String reqId = RequestIdWebFilter.get(exchange);
                    log.warn("Rate limit exceeded [reqId={}] ip='{}' path='{}'", reqId, clientIp, path);
                    return rejectWith429(exchange, reqId);
                });
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getHostString() : "unknown";
    }

    private Mono<Void> rejectWith429(ServerWebExchange exchange, String reqId) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().set("Retry-After",
                String.valueOf(props.refillPeriodSeconds()));

        try {
            byte[] body = objectMapper.writeValueAsBytes(
                    ResponseErrorDto.builder()
                            .error(ErrorMessageDto.builder()
                                    .message("Too many requests. Please try again later.")
                                    .build())
                            .requestId(reqId)
                            .build());
            return exchange.getResponse().writeWith(
                    Mono.just(dataBufferFactory.wrap(body)));
        } catch (JacksonException e) {
            log.error("Failed to serialize rate-limit error body [reqId={}]", reqId, e);
            return exchange.getResponse().setComplete();
        }
    }
}
