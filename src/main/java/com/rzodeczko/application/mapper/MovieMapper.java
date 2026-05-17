package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.MovieDto;
import com.rzodeczko.domain.movie.Movie;

public final class MovieMapper {

    private MovieMapper() {
    }

    public static MovieDto toDto(Movie movie) {
        if (movie == null) {
            return null;
        }
        return MovieDto.builder()
                .id(movie.id())
                .name(movie.name())
                .genre(movie.getGenre())
                .premiereDate(movie.premiereDate())
                .duration(movie.duration())
                .build();
    }
}
