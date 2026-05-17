package com.rzodeczko.domain.user;

import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.domain.generic.GenericEntity;
import com.rzodeczko.domain.movie.Movie;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;

/**
 * Single user entity. {@code role} controls authorization.
 */
public record User(
        String id,
        String username,
        String password,
        Role role,
        LocalDate birthDate,
        List<Movie> favoriteMovies,
        String email
) implements GenericEntity {

    public User() {
        this(null, null, null, Role.ROLE_USER, null, null, null);
    }

    public User(String id, String username, String password, Role role,
                LocalDate birthDate, List<Movie> favoriteMovies, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role != null ? role : Role.ROLE_USER;
        this.birthDate = birthDate;
        this.favoriteMovies = favoriteMovies == null ? new ArrayList<>() : new ArrayList<>(favoriteMovies);
        this.email = email;
    }

    public User(String username, String password, Role role,
                LocalDate birthDate, List<Movie> favoriteMovies, String email) {
        this(null, username, password, role, birthDate, favoriteMovies, email);
    }

    public User withId(String id) {
        return new User(id, username, password, role, birthDate, favoriteMovies, email);
    }

    public User withUsername(String username) {
        return new User(id, username, password, role, birthDate, favoriteMovies, email);
    }

    public User withPassword(String password) {
        return new User(id, username, password, role, birthDate, favoriteMovies, email);
    }

    public User withRole(Role role) {
        return new User(id, username, password, role, birthDate, favoriteMovies, email);
    }

    public User withBirthDate(LocalDate birthDate) {
        return new User(id, username, password, role, birthDate, favoriteMovies, email);
    }

    public User withFavoriteMovies(List<Movie> favoriteMovies) {
        return new User(id, username, password, role, birthDate, favoriteMovies, email);
    }

    public User withEmail(String email) {
        return new User(id, username, password, role, birthDate, favoriteMovies, email);
    }

    public User addMovieToFavorites(Movie movie) {
        var movies = isNull(favoriteMovies) ? new ArrayList<Movie>() : new ArrayList<>(favoriteMovies);
        movies.add(movie);
        return withFavoriteMovies(movies);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User u)) return false;
        return Objects.equals(id, u.id)
                && Objects.equals(username, u.username)
                && Objects.equals(password, u.password)
                && role == u.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, role);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String username;
        private String password;
        private Role role = Role.ROLE_USER;
        private LocalDate birthDate;
        private List<Movie> favoriteMovies;
        private String email;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder birthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public Builder favoriteMovies(List<Movie> favoriteMovies) {
            this.favoriteMovies = favoriteMovies;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public User build() {
            return new User(id, username, password, role, birthDate, favoriteMovies, email);
        }
    }
}
