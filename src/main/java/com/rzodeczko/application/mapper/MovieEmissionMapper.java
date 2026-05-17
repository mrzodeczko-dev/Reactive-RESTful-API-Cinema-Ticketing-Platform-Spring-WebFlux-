package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.MovieEmissionDto;
import com.rzodeczko.domain.movie_emission.MovieEmission;

public final class MovieEmissionMapper {

    private MovieEmissionMapper() {
    }

    public static MovieEmissionDto toDto(MovieEmission me) {
        if (me == null) {
            return null;
        }
        return MovieEmissionDto.builder()
                .id(me.id())
                .movieId(me.movie() == null ? null : me.movie().id())
                .startTime(me.startDateTime())
                .cinemaHallId(me.cinemaHallId())
                .isPositionFree(me.isPositionFree())
                .baseTicketPrice(me.baseTicketPrice() == null ? null : me.baseTicketPrice().value().toString())
                .build();
    }
}
