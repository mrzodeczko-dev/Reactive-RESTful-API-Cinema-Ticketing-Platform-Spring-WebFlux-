package com.rzodeczko.infrastructure.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Generates (or reuses) a unique request id for every incoming HTTP request:
 * <p>Runs at the highest precedence so the id is available for every other filter and handler.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestIdWebFilter implements WebFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String ATTRIBUTE = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders().getFirst(HEADER);
        String requestId = (incoming != null && !incoming.isBlank()) ? incoming : UUID.randomUUID().toString();

        exchange.getAttributes().put(ATTRIBUTE, requestId);
        exchange.getResponse().getHeaders().set(HEADER, requestId);

        return chain.filter(exchange);
    }

    /**
     * Reads the request id stored by this filter. Returns {@code "unknown"} if not present (defensive).
     */
    public static String get(ServerWebExchange exchange) {
        Object value = exchange.getAttribute(ATTRIBUTE);
        return value instanceof String s ? s : "unknown";
    }
}