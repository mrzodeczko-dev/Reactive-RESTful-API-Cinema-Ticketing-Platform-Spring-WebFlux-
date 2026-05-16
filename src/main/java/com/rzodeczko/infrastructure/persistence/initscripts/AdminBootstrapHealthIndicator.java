package com.rzodeczko.infrastructure.persistence.initscripts;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component("adminBootstrap")
public class AdminBootstrapHealthIndicator implements ReactiveHealthIndicator {

    private final AdminBootstrapState state;

    public AdminBootstrapHealthIndicator(AdminBootstrapState state) {
        this.state = state;
    }

    @Override
    public Mono<Health> health() {
        if (state.isReady()) {
            return Mono.just(Health.up().withDetail("adminBootstrap", "ready").build());
        } else {
            return Mono.just(Health.down().withDetail("adminBootstrap", "not ready").build());
        }
    }
}