package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;
import com.rzodeczko.infrastructure.persistence.document.CinemaHallDocument;
import com.rzodeczko.infrastructure.persistence.document.MovieEmissionDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CinemaHallDocumentMapperTest {

    @Nested
    @DisplayName("toDocument()")
    class ToDocumentTests {

        @Test
        @DisplayName("Null domain: null document")
        void shouldReturnNullWhenDomainIsNull() {
            assertThat(CinemaHallDocumentMapper.toDocument(null)).isNull();
        }

        @Test
        @DisplayName("Cinema hall domain: positions and emissions mapped")
        void shouldMapDomainToDocument() {
            CinemaHallDocument document = CinemaHallDocumentMapper.toDocument(cinemaHall());

            assertThat(document.getId()).isEqualTo("hall-1");
            assertThat(document.getCinemaId()).isEqualTo("cinema-1");
            assertThat(document.getPositions()).containsExactly(new Position(1, 1), new Position(1, 2));
            assertThat(document.getMovieEmissions()).hasSize(1);
            assertThat(document.getMovieEmissions().getFirst().getId()).isEqualTo("emission-1");
        }

        @Test
        @DisplayName("Null list: null documents list")
        void shouldReturnNullDocumentsWhenListIsNull() {
            assertThat(CinemaHallDocumentMapper.toDocuments(null)).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain()")
    class ToDomainTests {

        @Test
        @DisplayName("Null document: null domain")
        void shouldReturnNullWhenDocumentIsNull() {
            assertThat(CinemaHallDocumentMapper.toDomain(null)).isNull();
        }

        @Test
        @DisplayName("Cinema hall document: positions and emissions mapped")
        void shouldMapDocumentToDomain() {
            CinemaHall domain = CinemaHallDocumentMapper.toDomain(cinemaHallDocument("hall-1"));

            assertThat(domain.id()).isEqualTo("hall-1");
            assertThat(domain.cinemaId()).isEqualTo("cinema-1");
            assertThat(domain.positions()).containsExactly(new Position(1, 1), new Position(1, 2));
            assertThat(domain.movieEmissions()).hasSize(1);
            assertThat(domain.movieEmissions().getFirst().id()).isEqualTo("emission-1");
        }

        @Test
        @DisplayName("Document list: domains preserve order")
        void shouldMapDocumentListToDomains() {
            List<CinemaHall> domains = CinemaHallDocumentMapper.toDomains(List.of(
                    cinemaHallDocument("hall-1"),
                    cinemaHallDocument("hall-2")
            ));

            assertThat(domains).extracting(CinemaHall::id)
                    .containsExactly("hall-1", "hall-2");
        }
    }

    private CinemaHall cinemaHall() {
        return CinemaHall.builder()
                .id("hall-1")
                .cinemaId("cinema-1")
                .positions(List.of(new Position(1, 1), new Position(1, 2)))
                .movieEmissions(List.of(emission()))
                .build();
    }

    private CinemaHallDocument cinemaHallDocument(String id) {
        return new CinemaHallDocument(
                id,
                List.of(new Position(1, 1), new Position(1, 2)),
                "cinema-1",
                List.of(emissionDocument())
        );
    }

    private MovieEmission emission() {
        return MovieEmission.builder()
                .id("emission-1")
                .startDateTime(LocalDateTime.of(2026, 6, 1, 20, 0))
                .baseTicketPrice(Money.of("35.00"))
                .cinemaHallId("hall-1")
                .build();
    }

    private MovieEmissionDocument emissionDocument() {
        return new MovieEmissionDocument(
                "emission-1",
                null,
                LocalDateTime.of(2026, 6, 1, 20, 0),
                Money.of("35.00"),
                "hall-1",
                null
        );
    }
}
