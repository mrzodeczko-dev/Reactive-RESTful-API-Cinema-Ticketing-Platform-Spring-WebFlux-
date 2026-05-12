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

import java.time.Duration;

/**
 * Idempotent bootstrap of the admin user. Runs once on every startup, converges on the
 * same DB state regardless of what's already there:
 * <ul>
 *   <li>no user with that username → insert a fresh user with {@code ROLE_ADMIN}</li>
 *   <li>user exists with USER role → promote in place (same id)</li>
 *   <li>user exists with ADMIN role → no-op</li>
 * </ul>
 *
 * <p>Blocks startup until bootstrap completes (max 30 s) so the app never accepts
 * requests before the admin is in place.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapper implements ApplicationRunner {

    private static final Duration BOOTSTRAP_TIMEOUT = Duration.ofSeconds(30);

    private final UserPort userPort;
    private final PasswordEncoderPort passwordEncoder;
    private final AppAdminCredentials adminCredentials;

    @Override
    public void run(ApplicationArguments args) {
        String username = adminCredentials.username();
        log.info("Bootstrapping admin user '{}'", username);

        Mono<User> result = userPort
                .findByUsername(username)
                .flatMap(existing -> {
                    if (existing.getRole() == Role.ROLE_ADMIN) {
                        log.info("Admin '{}' already present — no-op", username);
                        return Mono.just(existing);
                    }
                    log.info("User '{}' exists with role {} — promoting to ADMIN",
                            username, existing.getRole());
                    return userPort.addOrUpdate(existing.setRole(Role.ROLE_ADMIN));
                })
                .switchIfEmpty(Mono.defer(() -> userPort
                        .addOrUpdate(User.builder()
                                .username(username)
                                .password(passwordEncoder.encode(adminCredentials.password()))
                                .role(Role.ROLE_ADMIN)
                                .build())
                        .doOnNext(u -> log.info("Created bootstrap admin '{}'", username))));

        try {
            result.block(BOOTSTRAP_TIMEOUT);
        } catch (RuntimeException e) {
            log.error("Admin bootstrap failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}
