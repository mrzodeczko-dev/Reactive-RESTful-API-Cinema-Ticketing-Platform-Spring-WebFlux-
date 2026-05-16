package com.rzodeczko.application.mapper;

import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie.enums.MovieGenre;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MovieEmissionMapperTest {

    @Nested
    @DisplayName("toDto()")
    class ToDtoTests {

        @Test
        @DisplayName("Null emission: null DTO")
        void shouldReturnNullWhenEmissionIsNull() {
            assertThat(MovieEmissionMapper.toDto(null)).isNull();
        }

        @Test
        @DisplayName("Movie emission: all fields mapped")
        void shouldMapMovieEmissionToDto() {
            LocalDateTime startTime = LocalDateTime.of(2026, 6, 1, 20, 30);
            Map<Position, Boolean> positions = new LinkedHashMap<>();
            positions.put(new Position(1, 1), true);
            positions.put(new Position(1, 2), false);
            MovieEmission emission = MovieEmission.builder()
                    .id("emission-1")
                    .movie(movie())
                    .startDateTime(startTime)
                    .baseTicketPrice(Money.of("35.50"))
                    .cinemaHallId("hall-1")
                    .isPositionFree(positions)
                    .build();

            var dto = MovieEmissionMapper.toDto(emission);

            assertThat(dto.id()).isEqualTo("emission-1");
            assertThat(dto.movieId()).isEqualTo("movie-1");
            assertThat(dto.startTime()).isEqualTo(startTime);
            assertThat(dto.cinemaHallId()).isEqualTo("hall-1");
            assertThat(dto.isPositionFree()).containsExactlyEntriesOf(positions);
            assertThat(dto.baseTicketPrice()).isEqualTo("35.50");
        }

        @Test
        @DisplayName("Emission without movie: movie id mapped as null")
        void shouldMapNullMovieAsNullMovieId() {
            MovieEmission emission = MovieEmission.builder()
                    .id("emission-1")
                    .movie(null)
                    .build();

            assertThat(MovieEmissionMapper.toDto(emission).movieId()).isNull();
        }

        @Test
        @DisplayName("Emission without base price: price mapped as null")
        void shouldMapNullBasePriceAsNull() {
            MovieEmission emission = MovieEmission.builder()
                    .id("emission-1")
                    .baseTicketPrice(null)
                    .build();

            assertThat(MovieEmissionMapper.toDto(emission).baseTicketPrice()).isNull();
        }
    }

    private Movie movie() {
        return Movie.builder()
                .id("movie-1")
                .name("Quiet Storm")
                .genre(MovieGenre.DRAMA)
                .build();
    }
}
