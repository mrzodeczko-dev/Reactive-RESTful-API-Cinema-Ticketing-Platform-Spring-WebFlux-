package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.CinemaHallDto;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.vo.Position;

import java.util.function.Function;
import java.util.stream.Collectors;

public final class CinemaHallMapper {

    private CinemaHallMapper() {
    }

    public static CinemaHallDto toDto(CinemaHall ch) {
        if (ch == null) {
            return null;
        }
        return CinemaHallDto.builder()
                .id(ch.id())
                .cinemaId(ch.cinemaId())
                .movieEmissions(ch.movieEmissions() == null ? null :
                        ch.movieEmissions().stream()
                                .map(MovieEmissionMapper::toDto)
                                .collect(Collectors.toList()))
                .rowNo(getMaxNumber(ch, Position::rowNo))
                .colNo(getMaxNumber(ch, Position::colNo))
                .build();
    }

    private static Integer getMaxNumber(CinemaHall ch, Function<Position, Integer> f) {
        return ch.positions()
                .stream()
                .map(f)
                .reduce(1, (a, b) -> a > b ? a : b);
    }
}
