package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.CinemaDto;
import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.cinema_hall.CinemaHall;

import java.util.stream.Collectors;

public final class CinemaMapper {

    private CinemaMapper() {
    }

    public static CinemaDto toDto(Cinema cinema) {
        if (cinema == null) {
            return null;
        }
        return CinemaDto.builder()
                .id(cinema.id())
                .city(cinema.cityName())
                .street(cinema.street())
                .hallsCapacity(cinema.cinemaHalls() == null ? null :
                        cinema.cinemaHalls()
                                .stream()
                                .collect(Collectors.toMap(
                                        CinemaHall::id,
                                        e -> e.positions().size())))
                .build();
    }
}
