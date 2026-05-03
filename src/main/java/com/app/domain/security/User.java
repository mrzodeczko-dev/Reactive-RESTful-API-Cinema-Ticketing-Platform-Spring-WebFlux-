package com.app.domain.security;

import com.app.application.dto.UserDto;
import com.app.domain.movie.Movie;
import com.app.domain.security.enums.Role;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.*;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document("users")
public final class User extends BaseUser {

    private LocalDate birthDate;
    private List<Movie> favoriteMovies;
    private String email;

    public User(String username, String password, LocalDate birthDate, List<Movie> favoriteMovies, String email) {
        super(username, password, Role.ROLE_USER);
        this.birthDate = birthDate;
        this.favoriteMovies = favoriteMovies;
        this.email = email;
    }

    public static RegularUserBuilder builder() {
        return new RegularUserBuilder();
    }

    public static class RegularUserBuilder {

        private String username;
        private String password;
        private String email;
        private LocalDate birthDate;
        private List<Movie> favoriteMovies;

        public RegularUserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public RegularUserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public RegularUserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public RegularUserBuilder birthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public RegularUserBuilder username(List<Movie> favoriteMovies) {
            this.favoriteMovies = favoriteMovies;
            return this;
        }

        public User build() {
            return new User(username, password, birthDate, favoriteMovies, email);
        }
    }

    public User addMovieToFavorites(Movie movie) {
        if (isNull(favoriteMovies)) {
            favoriteMovies = new ArrayList<>();
        }
        favoriteMovies.add(movie);
        return this;
    }

    public UserDto toDto() {
        return UserDto.builder()
                .id(super.getId())
                .username(super.getUsername())
                .role(super.getRole().name())
                .birthDate(nonNull(birthDate) ? birthDate.toString() : null)
                .favoriteMovies(isNull(favoriteMovies) ? Collections.emptyList() : favoriteMovies
                        .stream()
                        .map(Movie::toDto)
                        .collect(Collectors.toList())
                )
                .email(email)
                .build();
    }
}
