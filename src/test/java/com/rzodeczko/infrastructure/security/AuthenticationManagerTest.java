package com.rzodeczko.infrastructure.security;

import com.rzodeczko.application.exception.AuthenticationException;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.domain.user.User;
import com.rzodeczko.infrastructure.security.tokens.AppTokensService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationManagerTest {

    @Mock
    private AppTokensService appTokensService;
    @Mock
    private UserPort userPort;

    @InjectMocks
    private AuthenticationManager authenticationManager;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("user-id-1")
                .username("jan@example.com")
                .password("hashed-pass")
                .role(Role.ROLE_ADMIN)
                .build();
    }

    @Nested
    @DisplayName("authenticate()")
    class AuthenticateTests {

        @Test
        @DisplayName("Happy path: valid token returns authenticated principal and authorities")
        void shouldAuthenticateValidToken() {
            when(appTokensService.isTokenValid("jwt-token")).thenReturn(true);
            when(appTokensService.getId("jwt-token")).thenReturn("user-id-1");
            when(userPort.findById("user-id-1")).thenReturn(Mono.just(user));

            StepVerifier.create(authenticationManager.authenticate(authenticationWithToken("jwt-token")))
                    .assertNext(authentication -> {
                        assertThat(authentication.getPrincipal()).isEqualTo("jan@example.com");
                        assertThat(authentication.getCredentials()).isNull();
                        assertThat(authentication.getAuthorities())
                                .extracting("authority")
                                .containsExactly("ROLE_ADMIN");
                        assertThat(authentication.isAuthenticated()).isTrue();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Missing credentials: AuthenticationException emitted")
        void shouldErrorWhenCredentialsAreMissing() {
            StepVerifier.create(authenticationManager.authenticate(authenticationWithToken(null)))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(AuthenticationException.class);
                        assertThat(ex.getMessage()).contains("Credentials not provided");
                    })
                    .verify();

            verifyNoInteractions(appTokensService, userPort);
        }

        @Test
        @DisplayName("Invalid token: AuthenticationException emitted, no DB call")
        void shouldErrorWhenTokenIsInvalid() {
            when(appTokensService.isTokenValid("jwt-token")).thenReturn(false);

            StepVerifier.create(authenticationManager.authenticate(authenticationWithToken("jwt-token")))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(AuthenticationException.class);
                        assertThat(ex.getMessage()).contains("TOKEN IS NOT VALID");
                    })
                    .verify();

            verifyNoInteractions(userPort);
        }

        @Test
        @DisplayName("Token parsing failure: AuthenticationException emitted")
        void shouldMapTokenValidationException() {
            when(appTokensService.isTokenValid("jwt-token")).thenThrow(new IllegalArgumentException("broken token"));

            StepVerifier.create(authenticationManager.authenticate(authenticationWithToken("jwt-token")))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(AuthenticationException.class);
                        assertThat(ex.getMessage()).contains("broken token");
                    })
                    .verify();
        }

        @Test
        @DisplayName("User not found: AuthenticationException emitted")
        void shouldErrorWhenUserNotFound() {
            when(appTokensService.isTokenValid("jwt-token")).thenReturn(true);
            when(appTokensService.getId("jwt-token")).thenReturn("missing-id");
            when(userPort.findById("missing-id")).thenReturn(Mono.empty());

            StepVerifier.create(authenticationManager.authenticate(authenticationWithToken("jwt-token")))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(AuthenticationException.class);
                        assertThat(ex.getMessage()).contains("Wrong username");
                    })
                    .verify();
        }
    }

    private UsernamePasswordAuthenticationToken authenticationWithToken(String token) {
        return new UsernamePasswordAuthenticationToken("", token);
    }
}
