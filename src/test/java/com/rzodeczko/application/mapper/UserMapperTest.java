package com.rzodeczko.application.mapper;

import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie.enums.MovieGenre;
import com.rzodeczko.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Nested
    @DisplayName("toDto()")
    class ToDtoTests {

        @Test
        @DisplayName("Null user: null DTO")
        void shouldReturnNullWhenUserIsNull() {
            assertThat(UserMapper.toDto(null)).isNull();
        }

        @Test
        @DisplayName("User: maps identity, role, birth date, email and favorite movies")
        void shouldMapUserToDto() {
            User user = User.builder()
                    .id("user-1")
                    .username("jan@example.com")
                    .password("hashed-pass")
                    .role(Role.ROLE_ADMIN)
                    .birthDate(LocalDate.of(1990, 1, 2))
                    .email("jan@example.com")
                    .favoriteMovies(List.of(movie("movie-1", "Quiet Storm")))
                    .build();

            var dto = UserMapper.toDto(user);

            assertThat(dto.id()).isEqualTo("user-1");
            assertThat(dto.username()).isEqualTo("jan@example.com");
            assertThat(dto.role()).isEqualTo("ROLE_ADMIN");
            assertThat(dto.birthDate()).isEqualTo("1990-01-02");
            assertThat(dto.email()).isEqualTo("jan@example.com");
            assertThat(dto.favoriteMovies()).hasSize(1);
            assertThat(dto.favoriteMovies().getFirst().id()).isEqualTo("movie-1");
            assertThat(dto.favoriteMovies().getFirst().name()).isEqualTo("Quiet Storm");
        }

        @Test
        @DisplayName("User without optional fields: null birth date and empty favorites")
        void shouldMapMissingOptionalFields() {
            User user = User.builder()
                    .id("user-1")
                    .username("jan@example.com")
                    .password("hashed-pass")
                    .email("jan@example.com")
                    .build();

            var dto = UserMapper.toDto(user);

            assertThat(dto.birthDate()).isNull();
            assertThat(dto.role()).isEqualTo("ROLE_USER");
            assertThat(dto.favoriteMovies()).isEmpty();
        }
    }

    private Movie movie(String id, String name) {
        return Movie.builder()
                .id(id)
                .name(name)
                .genre(MovieGenre.DRAMA)
                .duration(120)
                .premiereDate(LocalDate.of(2026, 6, 1))
                .build();
    }
}
