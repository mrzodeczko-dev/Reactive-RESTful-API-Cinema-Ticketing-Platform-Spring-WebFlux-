package com.rzodeczko.application.mapper;

import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.vo.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CinemaMapperTest {

    @Nested
    @DisplayName("toDto()")
    class ToDtoTests {

        @Test
        @DisplayName("Null cinema: null DTO")
        void shouldReturnNullWhenCinemaIsNull() {
            assertThat(CinemaMapper.toDto(null)).isNull();
        }

        @Test
        @DisplayName("Cinema: maps id, city, street and hall capacities")
        void shouldMapCinemaToDto() {
            Cinema cinema = Cinema.builder()
                    .id("cinema-1")
                    .cityId("city-1")
                    .street("Long Street")
                    .cinemaHalls(List.of(hall("hall-1", 2), hall("hall-2", 4)))
                    .build();

            var dto = CinemaMapper.toDto(cinema);

            assertThat(dto.id()).isEqualTo("cinema-1");
            assertThat(dto.city()).isEqualTo("city-1");
            assertThat(dto.street()).isEqualTo("Long Street");
            assertThat(dto.hallsCapacity())
                    .containsEntry("hall-1", 2)
                    .containsEntry("hall-2", 4);
        }

        @Test
        @DisplayName("Cinema without halls: empty capacity map")
        void shouldMapCinemaWithoutHallsToEmptyCapacityMap() {
            Cinema cinema = Cinema.builder()
                    .id("cinema-1")
                    .cityId("city-1")
                    .street("Long Street")
                    .build();

            assertThat(CinemaMapper.toDto(cinema).hallsCapacity()).isEmpty();
        }
    }

    private CinemaHall hall(String id, int positionsCount) {
        return CinemaHall.builder()
                .id(id)
                .positions(java.util.stream.IntStream.rangeClosed(1, positionsCount)
                        .mapToObj(col -> new Position(1, col))
                        .toList())
                .build();
    }
}
