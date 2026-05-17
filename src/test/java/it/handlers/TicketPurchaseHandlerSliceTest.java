package it.handlers;

import com.rzodeczko.application.dto.CreateTicketPurchaseDto;
import com.rzodeczko.application.dto.TicketPurchaseDto;
import com.rzodeczko.application.exception.TicketPurchaseServiceException;
import com.rzodeczko.application.service.TicketPurchaseService;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.presentation.routing.TicketPurchasesRouting;
import com.rzodeczko.presentation.routing.handlers.TicketPurchaseHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import({
        TicketPurchasesRouting.class,
        TicketPurchaseHandler.class,
        AbstractHandlerSliceTest.Configs.class
})
@ActiveProfiles("handlers")
class TicketPurchaseHandlerSliceTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private TicketPurchaseService ticketPurchaseService;

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private static TicketPurchaseDto samplePurchase(String id, String username) {
        return TicketPurchaseDto.builder()
                .id(id)
                .username(username)
                .purchaseDate(LocalDate.of(2024, 6, 1))
                .tickets(List.of())
                .ticketGroupType(TicketGroupType.NORMAL)
                .build();
    }

    private static CreateTicketPurchaseDto sampleCreateDto() {
        return CreateTicketPurchaseDto.builder()
                .movieEmissionId("me-1")
                .ticketsDetails(List.of())
                .ticketGroupType(TicketGroupType.NORMAL)
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /ticketPurchases  (direct purchase)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /ticketPurchases -> 201 + saved TicketPurchaseDto")
    void shouldCreateTicketPurchase() {
        TicketPurchaseDto saved = samplePurchase("tp-1", "user1");
        when(ticketPurchaseService.purchaseTicket(any(), any(CreateTicketPurchaseDto.class)))
                .thenReturn(Mono.just(saved));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("user1"))
                .post().uri("/ticketPurchases")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleCreateDto())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("tp-1")
                .jsonPath("$.username").isEqualTo("user1");
    }

    @Test
    @DisplayName("POST /ticketPurchases -> 500 when service throws")
    void shouldReturn5xxWhenPurchaseFails() {
        when(ticketPurchaseService.purchaseTicket(any(), any(CreateTicketPurchaseDto.class)))
                .thenReturn(Mono.error(new TicketPurchaseServiceException("No available seats")));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("user1"))
                .post().uri("/ticketPurchases")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleCreateDto())
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /ticketPurchases/ticketOrderId/{ticketOrderId}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /ticketPurchases/ticketOrderId/{id} -> 201 + TicketPurchaseDto")
    void shouldPurchaseFromOrder() {
        TicketPurchaseDto saved = samplePurchase("tp-2", "user1");
        when(ticketPurchaseService.purchaseTicketFromOrder(eq("user1"), eq("order-1")))
                .thenReturn(Mono.just(saved));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("user1"))
                .post().uri("/ticketPurchases/ticketOrderId/order-1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("tp-2");
    }

    @Test
    @DisplayName("POST /ticketPurchases/ticketOrderId/{id} -> 500 when order not found")
    void shouldReturn5xxWhenOrderNotFound() {
        when(ticketPurchaseService.purchaseTicketFromOrder(anyString(), eq("missing-order")))
                .thenReturn(Mono.error(new TicketPurchaseServiceException("Order not found")));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("user1"))
                .post().uri("/ticketPurchases/ticketOrderId/missing-order")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /ticketPurchases  (for logged user)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /ticketPurchases -> 200 + list for logged user")
    void shouldGetAllPurchasesForLoggedUser() {
        when(ticketPurchaseService.getAllTicketPurchasesByUser("user1"))
                .thenReturn(Flux.just(
                        samplePurchase("tp-1", "user1"),
                        samplePurchase("tp-2", "user1")));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("user1"))
                .get().uri("/ticketPurchases")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo("tp-1")
                .jsonPath("$[1].id").isEqualTo("tp-2");
    }

    @Test
    @DisplayName("GET /ticketPurchases -> 200 + empty list when user has no purchases")
    void shouldReturnEmptyWhenNoPurchases() {
        when(ticketPurchaseService.getAllTicketPurchasesByUser("user1"))
                .thenReturn(Flux.empty());

        client.mutateWith(SecurityMockServerConfigurers.mockUser("user1"))
                .get().uri("/ticketPurchases")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /ticketPurchases/city/{city}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /ticketPurchases/city/{city} -> 200 + filtered list")
    void shouldGetPurchasesByCity() {
        when(ticketPurchaseService.getAllTicketPurchasesByUserAndCity("user1", "Warsaw"))
                .thenReturn(Flux.just(samplePurchase("tp-1", "user1")));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("user1"))
                .get().uri("/ticketPurchases/city/Warsaw")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo("tp-1");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /ticketPurchases/cinemaId/{cinemaId}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /ticketPurchases/cinemaId/{cinemaId} -> 200 + list for logged user")
    void shouldGetPurchasesByCinemaForUser() {
        when(ticketPurchaseService.getAllTicketPurchasesByCinemaAndUsername("cinema-1", "user1"))
                .thenReturn(Flux.just(samplePurchase("tp-1", "user1")));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("user1"))
                .get().uri("/ticketPurchases/cinemaId/cinema-1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /ticketPurchases/movieId/{movieId}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /ticketPurchases/movieId/{movieId} -> 200 + list for logged user")
    void shouldGetPurchasesByMovieForLoggedUser() {
        when(ticketPurchaseService.getAllTicketPurchasesForUsernameAndMovieId("user1", "movie-1"))
                .thenReturn(Flux.just(samplePurchase("tp-1", "user1")));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("user1"))
                .get().uri("/ticketPurchases/movieId/movie-1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /admin/ticketPurchases
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /admin/ticketPurchases -> 200 + all purchases")
    void shouldGetAllPurchasesAsAdmin() {
        when(ticketPurchaseService.getAllTicketPurchases())
                .thenReturn(Flux.just(
                        samplePurchase("tp-1", "user1"),
                        samplePurchase("tp-2", "user2")));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("admin").roles("ADMIN"))
                .get().uri("/admin/ticketPurchases")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /admin/ticketPurchases/city/{city}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /admin/ticketPurchases/city/{city} -> 200 + list for given city")
    void shouldGetAllPurchasesByCityAsAdmin() {
        when(ticketPurchaseService.getAllTicketPurchasesByCity("Krakow"))
                .thenReturn(Flux.just(samplePurchase("tp-3", "user2")));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("admin").roles("ADMIN"))
                .get().uri("/admin/ticketPurchases/city/Krakow")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo("tp-3");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /admin/ticketPurchases/cinemaId/{cinemaId}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /admin/ticketPurchases/cinemaId/{cinemaId} -> 200 + list")
    void shouldGetAllPurchasesByCinemaAsAdmin() {
        when(ticketPurchaseService.getAllTicketPurchaseByCinema("cinema-1"))
                .thenReturn(Flux.just(samplePurchase("tp-1", "user1")));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("admin").roles("ADMIN"))
                .get().uri("/admin/ticketPurchases/cinemaId/cinema-1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /admin/ticketPurchases/dates
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /admin/ticketPurchases/dates?from=&to= -> 200 + list")
    void shouldGetAllPurchasesByDateAsAdmin() {
        when(ticketPurchaseService.getAllTicketPurchasesByDate("2024-01-01", "2024-12-31"))
                .thenReturn(Flux.just(samplePurchase("tp-1", "user1")));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("admin").roles("ADMIN"))
                .get().uri("/admin/ticketPurchases/dates?from=2024-01-01&to=2024-12-31")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1);
    }

    @Test
    @DisplayName("GET /admin/ticketPurchases/dates (no params) -> 200 + list")
    void shouldGetAllPurchasesByDateWithoutParamsAsAdmin() {
        when(ticketPurchaseService.getAllTicketPurchasesByDate(null, null))
                .thenReturn(Flux.just(
                        samplePurchase("tp-1", "user1"),
                        samplePurchase("tp-2", "user2")));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("admin").roles("ADMIN"))
                .get().uri("/admin/ticketPurchases/dates")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /admin/ticketPurchases/movieId/{movieId}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /admin/ticketPurchases/movieId/{movieId} -> 200 + list")
    void shouldGetAllPurchasesWithMovieIdAsAdmin() {
        when(ticketPurchaseService.getAllTicketPurchasesWithMovieId("movie-1"))
                .thenReturn(Flux.just(samplePurchase("tp-1", "user1")));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("admin").roles("ADMIN"))
                .get().uri("/admin/ticketPurchases/movieId/movie-1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /admin/ticketPurchases/cinemaHallId/{cinemaHallId}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /admin/ticketPurchases/cinemaHallId/{id} -> 200 + list")
    void shouldGetAllPurchasesByCinemaHallIdAsAdmin() {
        when(ticketPurchaseService.getAllTicketPurchasesByCinemaHallId("hall-1"))
                .thenReturn(Flux.just(samplePurchase("tp-1", "user1")));

        client.mutateWith(SecurityMockServerConfigurers.mockUser("admin").roles("ADMIN"))
                .get().uri("/admin/ticketPurchases/cinemaHallId/hall-1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1);
    }
}
