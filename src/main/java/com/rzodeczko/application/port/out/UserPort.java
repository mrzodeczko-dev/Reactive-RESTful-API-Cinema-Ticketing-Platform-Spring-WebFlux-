package com.rzodeczko.application.port.out;

import com.rzodeczko.domain.user.User;
import reactor.core.publisher.Mono;

public interface UserPort extends PersistencePort<User, String> {
    Mono<User> findByUsername(String username);

    Mono<User> findByEmail(String email);
}
