package it.handlers;

import com.rzodeczko.application.dto.CreateMailDto;
import com.rzodeczko.application.dto.CreateMailsDto;
import com.rzodeczko.application.dto.MailDto;
import com.rzodeczko.application.dto.SendEmailToSelfDto;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.application.service.EmailService;
import com.rzodeczko.domain.user.User;
import com.rzodeczko.presentation.routing.EmailRouting;
import com.rzodeczko.presentation.routing.handlers.EmailHandler;
import com.rzodeczko.presentation.routing.userprovider.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@WebFluxTest
@Import({
        EmailRouting.class,
        EmailHandler.class,
        AbstractHandlerSliceTest.Configs.class
})
@ActiveProfiles("handlers")
class EmailHandlerSliceTest {

    @Autowired
    ApplicationContext context;

    private WebTestClient client;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private UserPort userPort;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    private User sampleUser(String username, String email) {
        return User.builder()
                .id("u-1")
                .username(username)
                .email(email)
                .build();
    }

    @Test
    @DisplayName("POST /emails/send/single → 201 + sent MailDto")
    void shouldSendSingleEmailToLoggedUser() {
        User user = sampleUser("user1", "user1@example.com");
        MailDto mailDto = MailDto.builder().to("user1@example.com").title("Hello").build();

        when(currentUserProvider.username()).thenReturn(Mono.just("user1"));
        when(userPort.findByUsername("user1")).thenReturn(Mono.just(user));
        when(emailService.sendEmailToSelf(any(SendEmailToSelfDto.class), eq("user1@example.com")))
                .thenReturn(Mono.just(mailDto));

        client.post().uri("/emails/send/single")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(SendEmailToSelfDto.builder()
                        .title("Hello")
                        .htmlContent("<p>Test</p>")
                        .build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.to").isEqualTo("user1@example.com")
                .jsonPath("$.title").isEqualTo("Hello");
    }

    @Test
    @DisplayName("POST /emails/send/multiple → 201 + list of sent mails")
    void shouldSendMultipleEmails() {
        MailDto mail1 = MailDto.builder().to("a@example.com").title("Hi A").build();
        MailDto mail2 = MailDto.builder().to("b@example.com").title("Hi B").build();

        when(emailService.sendMultipleEmails(any(CreateMailsDto.class)))
                .thenReturn(Flux.just(mail1, mail2));

        client.post().uri("/emails/send/multiple")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CreateMailsDto.builder()
                        .mails(List.of(
                                new CreateMailDto("a@example.com", "<p>A</p>", "Hi A"),
                                new CreateMailDto("b@example.com", "<p>B</p>", "Hi B")
                        ))
                        .build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].to").isEqualTo("a@example.com")
                .jsonPath("$[1].to").isEqualTo("b@example.com");
    }
}