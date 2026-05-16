package com.rzodeczko.application.mapper;

import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie.enums.MovieGenre;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MovieMapperTest {

    @Nested
    @DisplayName("toDto()")
    class ToDtoTests {

        @Test
        @DisplayName("Null movie: null DTO")
        void shouldReturnNullWhenMovieIsNull() {
            assertThat(MovieMapper.toDto(null)).isNull();
        }

        @Test
        @DisplayName("Movie: all fields mapped")
        void shouldMapMovieToDto() {
            Movie movie = Movie.builder()
                    .id("movie-1")
                    .name("Quiet Storm")
                    .genre(MovieGenre.DRAMA)
                    .duration(120)
                    .premiereDate(LocalDate.of(2026, 6, 1))
                    .build();

            var dto = MovieMapper.toDto(movie);

            assertThat(dto.id()).isEqualTo("movie-1");
            assertThat(dto.name()).isEqualTo("Quiet Storm");
            assertThat(dto.genre()).isEqualTo("Drama");
            assertThat(dto.duration()).isEqualTo(120);
            assertThat(dto.premiereDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        }

        @Test
        @DisplayName("Movie without genre: genre mapped as null")
        void shouldMapNullGenreAsNull() {
            Movie movie = Movie.builder()
                    .id("movie-1")
                    .name("Quiet Storm")
                    .duration(120)
                    .premiereDate(LocalDate.of(2026, 6, 1))
                    .build();

            assertThat(MovieMapper.toDto(movie).genre()).isNull();
        }
    }
}
