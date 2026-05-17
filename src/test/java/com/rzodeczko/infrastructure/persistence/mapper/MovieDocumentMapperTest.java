package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie.enums.MovieGenre;
import com.rzodeczko.infrastructure.persistence.document.MovieDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MovieDocumentMapperTest {

    @Nested
    @DisplayName("toDocument()")
    class ToDocumentTests {

        @Test
        @DisplayName("Null domain: null document")
        void shouldReturnNullWhenDomainIsNull() {
            assertThat(MovieDocumentMapper.toDocument(null)).isNull();
        }

        @Test
        @DisplayName("Movie domain: all fields mapped")
        void shouldMapDomainToDocument() {
            Movie movie = movie();

            MovieDocument document = MovieDocumentMapper.toDocument(movie);

            assertThat(document.getId()).isEqualTo("movie-1");
            assertThat(document.getName()).isEqualTo("Quiet Storm");
            assertThat(document.getGenre()).isEqualTo("Drama");
            assertThat(document.getDuration()).isEqualTo(120);
            assertThat(document.getPremiereDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        }
    }

    @Nested
    @DisplayName("toDomain()")
    class ToDomainTests {

        @Test
        @DisplayName("Null document: null domain")
        void shouldReturnNullWhenDocumentIsNull() {
            assertThat(MovieDocumentMapper.toDomain(null)).isNull();
        }

        @Test
        @DisplayName("Movie document: all fields mapped")
        void shouldMapDocumentToDomain() {
            MovieDocument document = new MovieDocument(
                    "movie-1",
                    "Quiet Storm",
                    "Drama",
                    120,
                    LocalDate.of(2026, 6, 1)
            );

            Movie movie = MovieDocumentMapper.toDomain(document);

            assertThat(movie.id()).isEqualTo("movie-1");
            assertThat(movie.name()).isEqualTo("Quiet Storm");
            assertThat(movie.getGenre()).isEqualTo("Drama");
            assertThat(movie.duration()).isEqualTo(120);
            assertThat(movie.premiereDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        }
    }

    private Movie movie() {
        return Movie.builder()
                .id("movie-1")
                .name("Quiet Storm")
                .genre(MovieGenre.DRAMA)
                .duration(120)
                .premiereDate(LocalDate.of(2026, 6, 1))
                .build();
    }
}
