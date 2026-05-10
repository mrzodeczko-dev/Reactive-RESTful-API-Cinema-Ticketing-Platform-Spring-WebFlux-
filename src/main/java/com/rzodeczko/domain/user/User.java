package com.rzodeczko.domain.user;

import com.rzodeczko.domain.generic.GenericEntity;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.application.security.enums.Role;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;

/**
 * Single user entity. {@code role} controls authorization — promote to {@code ROLE_ADMIN}
 * via {@link #setRole(Role)} (used by {@code AdminBootstrapper} and {@code UsersService}).
 */
public final class User implements GenericEntity {

    private String id;
    private String username;
    private String password;
    private Role role;
    private LocalDate birthDate;
    private List<Movie> favoriteMovies;
    private String email;

    public User() {
        this.role = Role.ROLE_USER;
    }

    public User(String username, String password, Role role,
                LocalDate birthDate, List<Movie> favoriteMovies, String email) {
        this.username = username;
        this.password = password;
        this.role = role != null ? role : Role.ROLE_USER;
        this.birthDate = birthDate;
        this.favoriteMovies = favoriteMovies;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public List<Movie> getFavoriteMovies() {
        return favoriteMovies;
    }

    public void setFavoriteMovies(List<Movie> favoriteMovies) {
        this.favoriteMovies = favoriteMovies;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Mutates the favorites list. Returns {@code this} for fluent chaining.
     */
    public User addMovieToFavorites(Movie movie) {
        if (isNull(favoriteMovies)) {
            favoriteMovies = new ArrayList<>();
        }
        favoriteMovies.add(movie);
        return this;
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
        private String username;
        private String password;
        private Role role = Role.ROLE_USER;
        private LocalDate birthDate;
        private List<Movie> favoriteMovies;
        private String email;

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
            return new User(username, password, role, birthDate, favoriteMovies, email);
        }
    }
}