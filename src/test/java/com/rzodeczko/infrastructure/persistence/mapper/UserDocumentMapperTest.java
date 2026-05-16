package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie.enums.MovieGenre;
import com.rzodeczko.domain.user.User;
import com.rzodeczko.infrastructure.persistence.document.MovieDocument;
import com.rzodeczko.infrastructure.persistence.document.UserDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserDocumentMapperTest {

    @Nested
    @DisplayName("toDocument()")
    class ToDocumentTests {

        @Test
        @DisplayName("Null domain: null document")
        void shouldReturnNullWhenDomainIsNull() {
            assertThat(UserDocumentMapper.toDocument(null)).isNull();
        }

        @Test
        @DisplayName("User domain: all fields and favorite movies mapped")
        void shouldMapDomainToDocument() {
            User user = user();

            UserDocument document = UserDocumentMapper.toDocument(user);

            assertThat(document.getId()).isEqualTo("user-1");
            assertThat(document.getUsername()).isEqualTo("jan@example.com");
            assertThat(document.getPassword()).isEqualTo("hashed-pass");
            assertThat(document.getRole()).isEqualTo(Role.ROLE_ADMIN);
            assertThat(document.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 2));
            assertThat(document.getEmail()).isEqualTo("jan@example.com");
            assertThat(document.getFavoriteMovies()).hasSize(1);
            assertThat(document.getFavoriteMovies().getFirst().getId()).isEqualTo("movie-1");
        }
    }

    @Nested
    @DisplayName("toDomain()")
    class ToDomainTests {

        @Test
        @DisplayName("Null document: null domain")
        void shouldReturnNullWhenDocumentIsNull() {
            assertThat(UserDocumentMapper.toDomain(null)).isNull();
        }

        @Test
        @DisplayName("User document: all fields and favorite movies mapped")
        void shouldMapDocumentToDomain() {
            User domain = UserDocumentMapper.toDomain(userDocument(Role.ROLE_ADMIN));

            assertThat(domain.getId()).isEqualTo("user-1");
            assertThat(domain.getUsername()).isEqualTo("jan@example.com");
            assertThat(domain.getPassword()).isEqualTo("hashed-pass");
            assertThat(domain.getRole()).isEqualTo(Role.ROLE_ADMIN);
            assertThat(domain.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 2));
            assertThat(domain.getEmail()).isEqualTo("jan@example.com");
            assertThat(domain.getFavoriteMovies()).hasSize(1);
            assertThat(domain.getFavoriteMovies().getFirst().getId()).isEqualTo("movie-1");
        }

        @Test
        @DisplayName("Document without role: defaults to ROLE_USER")
        void shouldDefaultMissingRoleToUser() {
            User domain = UserDocumentMapper.toDomain(userDocument(null));

            assertThat(domain.getRole()).isEqualTo(Role.ROLE_USER);
        }
    }

    private User user() {
        return User.builder()
                .id("user-1")
                .username("jan@example.com")
                .password("hashed-pass")
                .role(Role.ROLE_ADMIN)
                .birthDate(LocalDate.of(1990, 1, 2))
                .favoriteMovies(List.of(movie()))
                .email("jan@example.com")
                .build();
    }

    private UserDocument userDocument(Role role) {
        return new UserDocument(
                "user-1",
                "jan@example.com",
                "hashed-pass",
                role,
                LocalDate.of(1990, 1, 2),
                List.of(movieDocument()),
                "jan@example.com"
        );
    }

    private Movie movie() {
        return Movie.builder()
                .id("movie-1")
                .name("Quiet Storm")
                .genre(MovieGenre.DRAMA)
                .duration(120)
                .premiereDate(LocalDate.of(2026, 6, 1))
                .build();
    }

    private MovieDocument movieDocument() {
        return new MovieDocument("movie-1", "Quiet Storm", "Drama", 120, LocalDate.of(2026, 6, 1));
    }
}
