package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie.enums.MovieGenre;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;
import com.rzodeczko.infrastructure.persistence.document.MovieDocument;
import com.rzodeczko.infrastructure.persistence.document.MovieEmissionDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MovieEmissionDocumentMapperTest {

    @Nested
    @DisplayName("toDocument()")
    class ToDocumentTests {

        @Test
        @DisplayName("Null domain: null document")
        void shouldReturnNullWhenDomainIsNull() {
            assertThat(MovieEmissionDocumentMapper.toDocument(null)).isNull();
        }

        @Test
        @DisplayName("Movie emission domain: nested movie and all fields mapped")
        void shouldMapDomainToDocument() {
            MovieEmission emission = emission();

            MovieEmissionDocument document = MovieEmissionDocumentMapper.toDocument(emission);

            assertThat(document.getId()).isEqualTo("emission-1");
            assertThat(document.getMovie().getId()).isEqualTo("movie-1");
            assertThat(document.getStartDateTime()).isEqualTo(startTime());
            assertThat(document.getBaseTicketPrice()).isEqualTo(Money.of("35.50"));
            assertThat(document.getCinemaHallId()).isEqualTo("hall-1");
            assertThat(document.getIsPositionFree()).containsExactlyEntriesOf(positionsMap());
        }

        @Test
        @DisplayName("Null list: null documents list")
        void shouldReturnNullDocumentsWhenListIsNull() {
            assertThat(MovieEmissionDocumentMapper.toDocuments(null)).isNull();
        }

        @Test
        @DisplayName("Emission list: documents preserve order")
        void shouldMapDomainListToDocuments() {
            List<MovieEmissionDocument> documents = MovieEmissionDocumentMapper.toDocuments(List.of(
                    emission().withId("emission-1"),
                    emission().withId("emission-2")
            ));

            assertThat(documents).extracting(MovieEmissionDocument::getId)
                    .containsExactly("emission-1", "emission-2");
        }
    }

    @Nested
    @DisplayName("toDomain()")
    class ToDomainTests {

        @Test
        @DisplayName("Null document: null domain")
        void shouldReturnNullWhenDocumentIsNull() {
            assertThat(MovieEmissionDocumentMapper.toDomain(null)).isNull();
        }

        @Test
        @DisplayName("Movie emission document: nested movie and all fields mapped")
        void shouldMapDocumentToDomain() {
            MovieEmissionDocument document = new MovieEmissionDocument(
                    "emission-1",
                    movieDocument(),
                    startTime(),
                    Money.of("35.50"),
                    "hall-1",
                    positionsMap()
            );

            MovieEmission emission = MovieEmissionDocumentMapper.toDomain(document);

            assertThat(emission.id()).isEqualTo("emission-1");
            assertThat(emission.movie().id()).isEqualTo("movie-1");
            assertThat(emission.startDateTime()).isEqualTo(startTime());
            assertThat(emission.baseTicketPrice()).isEqualTo(Money.of("35.50"));
            assertThat(emission.cinemaHallId()).isEqualTo("hall-1");
            assertThat(emission.isPositionFree()).containsExactlyEntriesOf(positionsMap());
        }

        @Test
        @DisplayName("Null list: null domains list")
        void shouldReturnNullDomainsWhenListIsNull() {
            assertThat(MovieEmissionDocumentMapper.toDomains(null)).isNull();
        }

        @Test
        @DisplayName("Document list: domains preserve order")
        void shouldMapDocumentListToDomains() {
            List<MovieEmission> emissions = MovieEmissionDocumentMapper.toDomains(List.of(
                    document("emission-1"),
                    document("emission-2")
            ));

            assertThat(emissions).extracting(MovieEmission::id)
                    .containsExactly("emission-1", "emission-2");
        }
    }

    private MovieEmission emission() {
        return MovieEmission.builder()
                .id("emission-1")
                .movie(Movie.builder()
                        .id("movie-1")
                        .name("Quiet Storm")
                        .genre(MovieGenre.DRAMA)
                        .duration(120)
                        .premiereDate(LocalDate.of(2026, 6, 1))
                        .build())
                .startDateTime(startTime())
                .baseTicketPrice(Money.of("35.50"))
                .cinemaHallId("hall-1")
                .isPositionFree(positionsMap())
                .build();
    }

    private MovieEmissionDocument document(String id) {
        return new MovieEmissionDocument(id, movieDocument(), startTime(), Money.of("35.50"), "hall-1", positionsMap());
    }

    private MovieDocument movieDocument() {
        return new MovieDocument("movie-1", "Quiet Storm", "Drama", 120, LocalDate.of(2026, 6, 1));
    }

    private LocalDateTime startTime() {
        return LocalDateTime.of(2026, 6, 1, 20, 30);
    }

    private Map<Position, Boolean> positionsMap() {
        Map<Position, Boolean> positions = new LinkedHashMap<>();
        positions.put(new Position(1, 1), true);
        positions.put(new Position(1, 2), false);
        return positions;
    }
}
