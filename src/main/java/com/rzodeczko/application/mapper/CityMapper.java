package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.CinemaInCityDto;
import com.rzodeczko.application.dto.CityDto;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.city.City;

import java.util.stream.Collectors;

public final class CityMapper {

    private CityMapper() {
    }

    public static CityDto toDto(City city) {
        if (city == null) {
            return null;
        }
        return CityDto.builder()
                .id(city.id())
                .name(city.name())
                .cinemas(city.cinemas() == null ? null :
                        city.cinemas().stream()
                                .map(cinema -> CinemaInCityDto.builder()
                                        .id(cinema.id())
                                        .cinemaHallsCapacities(cinema.cinemaHalls() == null ? null :
                                                cinema.cinemaHalls()
                                                        .stream()
                                                        .collect(Collectors.toMap(
                                                                CinemaHall::id,
                                                                e -> e.positions().size())))
                                        .build())
                                .collect(Collectors.toList()))
                .build();
    }
}
