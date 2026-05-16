package com.rzodeczko.application.mapper;

import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.city.City;
import com.rzodeczko.domain.vo.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CityMapperTest {

    @Nested
    @DisplayName("toDto()")
    class ToDtoTests {

        @Test
        @DisplayName("Null city: null DTO")
        void shouldReturnNullWhenCityIsNull() {
            assertThat(CityMapper.toDto(null)).isNull();
        }

        @Test
        @DisplayName("City with cinemas: maps cinema ids and hall capacities")
        void shouldMapCityWithCinemasToDto() {
            City city = City.builder()
                    .id("city-1")
                    .name("Warsaw")
                    .cinemas(List.of(
                            cinema("cinema-1", hall("hall-1", 2), hall("hall-2", 3)),
                            cinema("cinema-2", hall("hall-3", 4))
                    ))
                    .build();

            var dto = CityMapper.toDto(city);

            assertThat(dto.id()).isEqualTo("city-1");
            assertThat(dto.name()).isEqualTo("Warsaw");
            assertThat(dto.cinemas()).hasSize(2);
            assertThat(dto.cinemas().getFirst().id()).isEqualTo("cinema-1");
            assertThat(dto.cinemas().getFirst().cinemaHallsCapacities())
                    .containsEntry("hall-1", 2)
                    .containsEntry("hall-2", 3);
            assertThat(dto.cinemas().get(1).id()).isEqualTo("cinema-2");
            assertThat(dto.cinemas().get(1).cinemaHallsCapacities())
                    .containsEntry("hall-3", 4);
        }

        @Test
        @DisplayName("City without cinemas: empty cinemas list")
        void shouldMapCityWithoutCinemasToEmptyList() {
            City city = City.builder()
                    .id("city-1")
                    .name("Warsaw")
                    .build();

            assertThat(CityMapper.toDto(city).cinemas()).isEmpty();
        }
    }

    private Cinema cinema(String id, CinemaHall... halls) {
        return Cinema.builder()
                .id(id)
                .cityId("city-1")
                .street("Long Street")
                .cinemaHalls(List.of(halls))
                .build();
    }

    private CinemaHall hall(String id, int positionsCount) {
        return CinemaHall.builder()
                .id(id)
                .positions(positions(positionsCount))
                .build();
    }

    private List<Position> positions(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(col -> new Position(1, col))
                .toList();
    }
}
