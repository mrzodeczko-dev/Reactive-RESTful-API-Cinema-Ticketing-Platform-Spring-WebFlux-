package com.rzodeczko.application.mapper;

import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie.enums.MovieGenre;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CinemaHallMapperTest {

    @Nested
    @DisplayName("toDto()")
    class ToDtoTests {

        @Test
        @DisplayName("Null cinema hall: null DTO")
        void shouldReturnNullWhenCinemaHallIsNull() {
            assertThat(CinemaHallMapper.toDto(null)).isNull();
        }

        @Test
        @DisplayName("Cinema hall: maps id, cinema id, max dimensions and emissions")
        void shouldMapCinemaHallToDto() {
            LocalDateTime startTime = LocalDateTime.of(2026, 6, 1, 20, 0);
            CinemaHall hall = CinemaHall.builder()
                    .id("hall-1")
                    .cinemaId("cinema-1")
                    .positions(List.of(
                            new Position(1, 1),
                            new Position(2, 3),
                            new Position(4, 2)
                    ))
                    .movieEmissions(List.of(emission("emission-1", startTime)))
                    .build();

            var dto = CinemaHallMapper.toDto(hall);

            assertThat(dto.id()).isEqualTo("hall-1");
            assertThat(dto.cinemaId()).isEqualTo("cinema-1");
            assertThat(dto.rowNo()).isEqualTo(4);
            assertThat(dto.colNo()).isEqualTo(3);
            assertThat(dto.movieEmissions()).hasSize(1);
            assertThat(dto.movieEmissions().getFirst().id()).isEqualTo("emission-1");
            assertThat(dto.movieEmissions().getFirst().startTime()).isEqualTo(startTime);
        }

        @Test
        @DisplayName("Cinema hall without positions: dimensions default to one")
        void shouldMapEmptyPositionsToDefaultDimensions() {
            CinemaHall hall = CinemaHall.builder()
                    .id("hall-1")
                    .cinemaId("cinema-1")
                    .build();

            var dto = CinemaHallMapper.toDto(hall);

            assertThat(dto.rowNo()).isEqualTo(1);
            assertThat(dto.colNo()).isEqualTo(1);
            assertThat(dto.movieEmissions()).isEmpty();
        }
    }

    private MovieEmission emission(String id, LocalDateTime startTime) {
        return MovieEmission.builder()
                .id(id)
                .movie(Movie.builder().id("movie-1").name("Quiet Storm").genre(MovieGenre.DRAMA).build())
                .startDateTime(startTime)
                .baseTicketPrice(Money.of("35.00"))
                .cinemaHallId("hall-1")
                .build();
    }
}
