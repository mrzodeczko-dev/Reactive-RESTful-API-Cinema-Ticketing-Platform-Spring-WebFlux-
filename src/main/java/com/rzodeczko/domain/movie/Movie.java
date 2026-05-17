package com.rzodeczko.domain.movie;

import com.rzodeczko.domain.generic.GenericEntity;
import com.rzodeczko.domain.movie.enums.MovieGenre;

import java.time.LocalDate;

public record Movie(
        String id,
        String name,
        MovieGenre genre,
        Integer duration,
        LocalDate premiereDate
) implements GenericEntity {

    public Movie() {
        this(null, null, null, null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Movie withId(String id) { return new Movie(id, name, genre, duration, premiereDate); }
    public Movie withName(String name) { return new Movie(id, name, genre, duration, premiereDate); }
    public String getGenre() { return genre == null ? null : genre.getDesc(); }
    public Movie withGenre(String genre) { return new Movie(id, name, MovieGenre.fromDesc(genre), duration, premiereDate); }
    public Movie withGenre(MovieGenre genre) { return new Movie(id, name, genre, duration, premiereDate); }
    public Movie withDuration(Integer duration) { return new Movie(id, name, genre, duration, premiereDate); }
    public Movie withPremiereDate(LocalDate premiereDate) { return new Movie(id, name, genre, duration, premiereDate); }

    public static class Builder {
        private String id;
        private String name;
        private MovieGenre genre;
        private Integer duration;
        private LocalDate premiereDate;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder genre(String genre) { this.genre = MovieGenre.fromDesc(genre); return this; }
        public Builder genre(MovieGenre genre) { this.genre = genre; return this; }
        public Builder duration(Integer duration) { this.duration = duration; return this; }
        public Builder premiereDate(LocalDate premiereDate) { this.premiereDate = premiereDate; return this; }
        public Movie build() { return new Movie(id, name, genre, duration, premiereDate); }
    }
}
