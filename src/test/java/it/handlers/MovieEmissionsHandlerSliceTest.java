package it.handlers;

import com.rzodeczko.application.dto.CreateMovieEmissionDto;
import com.rzodeczko.application.dto.MovieEmissionDto;
import com.rzodeczko.application.service.MovieEmissionService;
import com.rzodeczko.presentation.routing.MovieEmissionsRouting;
import com.rzodeczko.presentation.routing.handlers.MovieEmissionsHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import({
        MovieEmissionsRouting.class,
        MovieEmissionsHandler.class,
        AbstractHandlerSliceTest.Configs.class
})
@ActiveProfiles("handlers")
class MovieEmissionsHandlerSliceTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private MovieEmissionService movieEmissionService;

    private static MovieEmissionDto sampleEmission(String id, String movieId, String hallId) {
        return MovieEmissionDto.builder()
                .id(id)
                .movieId(movieId)
                .cinemaHallId(hallId)
                .startTime(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    @DisplayName("POST /movieEmissions → 201 + saved MovieEmissionDto")
    void shouldCreateMovieEmission() {
        MovieEmissionDto saved = sampleEmission("e-1", "m-1", "h-1");
        when(movieEmissionService.createMovieEmission(any())).thenReturn(Mono.just(saved));

        client.post().uri("/movieEmissions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CreateMovieEmissionDto.builder()
                        .movieId("m-1")
                        .cinemaHallId("h-1")
                        .startTime("2026-12-01T18:00:00")
                        .build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("e-1")
                .jsonPath("$.movieId").isEqualTo("m-1");
    }

    @Test
    @DisplayName("GET /movieEmissions → 200 + list of emissions")
    void shouldListAllEmissions() {
        when(movieEmissionService.getAllMovieEmissions()).thenReturn(Flux.just(
                sampleEmission("e-1", "m-1", "h-1"),
                sampleEmission("e-2", "m-2", "h-2")));

        client.get().uri("/movieEmissions")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo("e-1")
                .jsonPath("$[1].id").isEqualTo("e-2");
    }

    @Test
    @DisplayName("GET /movieEmissions → 200 + empty array when none")
    void shouldReturnEmptyListWhenNoEmissions() {
        when(movieEmissionService.getAllMovieEmissions()).thenReturn(Flux.empty());

        client.get().uri("/movieEmissions")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @DisplayName("GET /movieEmissions/movieId/{movieId} → 200 + emissions for movie")
    void shouldGetEmissionsByMovieId() {
        when(movieEmissionService.getAllMovieEmissionsByMovieId("m-1")).thenReturn(Flux.just(
                sampleEmission("e-1", "m-1", "h-1")));

        client.get().uri("/movieEmissions/movieId/m-1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].movieId").isEqualTo("m-1");
    }

    @Test
    @DisplayName("GET /movieEmissions/cinemaHallId/{cinemaHallId} → 200 + emissions for hall")
    void shouldGetEmissionsByCinemaHallId() {
        when(movieEmissionService.getAllMovieEmissionsByCinemaHallId("h-1")).thenReturn(Flux.just(
                sampleEmission("e-1", "m-1", "h-1"),
                sampleEmission("e-3", "m-3", "h-1")));

        client.get().uri("/movieEmissions/cinemaHallId/h-1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].cinemaHallId").isEqualTo("h-1");
    }

    @Test
    @DisplayName("DELETE /movieEmissions/{id} → 200 + deleted emission")
    void shouldDeleteMovieEmission() {
        MovieEmissionDto deleted = sampleEmission("e-1", "m-1", "h-1");
        when(movieEmissionService.deleteMovieEmission("e-1")).thenReturn(Mono.just(deleted));

        client.delete().uri("/movieEmissions/e-1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("e-1");
    }
}