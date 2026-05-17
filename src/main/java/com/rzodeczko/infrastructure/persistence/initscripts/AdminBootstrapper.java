package com.rzodeczko.infrastructure.persistence.initscripts;

import com.rzodeczko.application.port.out.PasswordEncoderPort;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.domain.user.User;
import com.rzodeczko.application.security.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapper implements ApplicationRunner {

    private static final Duration RETRY_INITIAL = Duration.ofSeconds(2);
    private static final int RETRY_MAX_ATTEMPTS = 3;

    private final UserPort userPort;
    private final PasswordEncoderPort passwordEncoder;
    private final AppAdminCredentials adminCredentials;
    private final AdminBootstrapState bootstrapState;

    @Override
    public void run(ApplicationArguments args) {
        String username = adminCredentials.username();
        log.info("Starting async admin bootstrap for '{}'", username);

        userPort
                .findByUsername(username)
                .flatMap(existing -> {
                    if (existing.role() == Role.ROLE_ADMIN) {
                        log.info("Admin '{}' already present — no-op", username);
                        return Mono.just(existing);
                    }
                    log.info("User '{}' exists with role {} — promoting to ADMIN", username, existing.role());
                    return userPort.addOrUpdate(existing.withRole(Role.ROLE_ADMIN));
                })
                .switchIfEmpty(Mono.defer(() -> userPort.addOrUpdate(User.builder()
                                .username(username)
                                .password(passwordEncoder.encode(adminCredentials.password()))
                                .role(Role.ROLE_ADMIN)
                                .build())
                        .doOnNext(u -> log.info("Created bootstrap admin '{}'", username))
                ))

                .retryWhen(Retry.backoff(RETRY_MAX_ATTEMPTS, RETRY_INITIAL)
                        .maxBackoff(Duration.ofSeconds(10))
                        .jitter(0.2)
                        .doBeforeRetry(rs -> log.warn("Retry admin bootstrap attempt #{} due to {}", rs.totalRetries() + 1, rs.failure().getMessage()))
                )
                .doOnError(e -> {
                    log.error("Admin bootstrap failed after retries: {}", e.getMessage(), e);
                    bootstrapState.markNotReady();
                })
                .doOnSuccess(u -> {
                    log.info("Admin bootstrap completed successfully for '{}'", username);
                    bootstrapState.markReady();
                })
                .subscribe(); // non-blocking: do not block startup thread
    }
}