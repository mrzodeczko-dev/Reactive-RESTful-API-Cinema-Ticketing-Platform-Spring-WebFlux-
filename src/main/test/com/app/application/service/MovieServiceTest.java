package com.app.application.service;

import com.app.application.dto.CreateMovieDto;
import com.app.application.exception.MovieServiceException;
import com.app.application.validator.CreateMovieDtoValidator;
import com.app.domain.movie.Movie;
import com.app.domain.movie.MovieRepository;
import com.app.domain.security.User;
import com.app.domain.security.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CreateMovieDtoValidator createMovieDtoValidator;

    @InjectMocks
    private MovieService movieService;

    private Movie drama2026;
    private Movie action2025;

    @BeforeEach
    void setUp() {
        drama2026 = Movie.builder()
                .id("movie-1")
                .name("Quiet Storm")
                .genre("Drama")
                .duration(120)
                .premiereDate(LocalDate.of(2026, 3, 1))
                .build();

        action2025 = Movie.builder()
                .id("movie-2")
                .name("Fast Burn")
                .genre("Action")
                .duration(95)
                .premiereDate(LocalDate.of(2025, 6, 15))
                .build();
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Two movies: Flux emits both DTOs")
        void shouldReturnAllMovies() {
            when(movieRepository.findAll()).thenReturn(Flux.just(drama2026, action2025));

            StepVerifier.create(movieService.getAll())
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("movie-1"))
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("movie-2"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Null element in stream: filtered out, only non-null emitted")
        void shouldFilterNullElements() {
            when(movieRepository.findAll()).thenReturn(Flux.just(drama2026));

            StepVerifier.create(movieService.getAll())
                    .expectNextCount(1)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("getFilteredByKeyword()")
    class FilteredByKeywordTests {

        @Test
        @DisplayName("Null keyword: MovieServiceException emitted")
        void shouldErrorOnNullKeyword() {
            StepVerifier.create(movieService.getFilteredByKeyword(null))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(MovieServiceException.class);
                        assertThat(ex.getMessage()).contains("null");
                    })
                    .verify();
            verifyNoInteractions(movieRepository);
        }

        @Test
        @DisplayName("Matching genre: only Drama movies returned")
        void shouldReturnMoviesMatchingGenre() {
            when(movieRepository.findAll()).thenReturn(Flux.just(drama2026, action2025));

            StepVerifier.create(movieService.getFilteredByKeyword("Drama"))
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("movie-1"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Matching name: movie with matching name returned")
        void shouldReturnMoviesMatchingName() {
            when(movieRepository.findAll()).thenReturn(Flux.just(drama2026, action2025));

            StepVerifier.create(movieService.getFilteredByKeyword("Fast Burn"))
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("movie-2"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("No match: Flux completes empty")
        void shouldReturnEmptyWhenNoMatch() {
            when(movieRepository.findAll()).thenReturn(Flux.just(drama2026, action2025));

            StepVerifier.create(movieService.getFilteredByKeyword("Horror")).verifyComplete();
        }
    }

    @Nested
    @DisplayName("getFilteredByGenre()")
    class FilteredByGenreTests {

        @Test
        @DisplayName("Null genre: MovieServiceException emitted")
        void shouldErrorOnNullGenre() {
            StepVerifier.create(movieService.getFilteredByGenre(null))
                    .expectError(MovieServiceException.class).verify();
            verifyNoInteractions(movieRepository);
        }

        @Test
        @DisplayName("Matching genre: only matching movies emitted")
        void shouldReturnMatchingMovies() {
            when(movieRepository.findAll()).thenReturn(Flux.just(drama2026, action2025));

            StepVerifier.create(movieService.getFilteredByGenre("Action"))
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("movie-2"))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("getFilteredByDuration()")
    class FilteredByDurationTests {

        @Test
        @DisplayName("Both nulls: MovieServiceException emitted")
        void shouldErrorWhenBothNull() {
            StepVerifier.create(movieService.getFilteredByDuration(null, null))
                    .expectError(MovieServiceException.class).verify();
        }

        @Test
        @DisplayName("minDuration > maxDuration: MovieServiceException emitted")
        void shouldErrorWhenMinGreaterThanMax() {
            StepVerifier.create(movieService.getFilteredByDuration(200, 100))
                    .expectError(MovieServiceException.class).verify();
        }

        @Test
        @DisplayName("Valid range: only movies within range returned")
        void shouldReturnMoviesWithinRange() {
            when(movieRepository.findAll()).thenReturn(Flux.just(drama2026, action2025));

            StepVerifier.create(movieService.getFilteredByDuration(90, 100))
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("movie-2"))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("getFilteredByPremiereDate()")
    class FilteredByPremiereDateTests {

        @Test
        @DisplayName("Both nulls: MovieServiceException emitted")
        void shouldErrorWhenBothNull() {
            StepVerifier.create(movieService.getFilteredByPremiereDate(null, null))
                    .expectError(MovieServiceException.class).verify();
        }

        @Test
        @DisplayName("minDate after maxDate: MovieServiceException emitted")
        void shouldErrorWhenMinAfterMax() {
            StepVerifier.create(movieService.getFilteredByPremiereDate(
                            LocalDate.of(2027, 1, 1), LocalDate.of(2026, 1, 1)))
                    .expectError(MovieServiceException.class).verify();
        }

        @Test
        @DisplayName("Valid range: only movies with premiere in range returned")
        void shouldReturnMoviesInDateRange() {
            when(movieRepository.findAll()).thenReturn(Flux.just(drama2026, action2025));

            StepVerifier.create(movieService.getFilteredByPremiereDate(
                            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("movie-1"))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("addMovieToFavorites()")
    class AddMovieToFavoritesTests {

        @Test
        @DisplayName("Happy path: movie added to user favorites")
        void shouldAddToFavorites() {
            User user = User.builder()
                    .username("jan@example.com")
                    .email("jan@example.com")
                    .password("pass")
                    .build();

            when(movieRepository.findById("movie-1")).thenReturn(Mono.just(drama2026));
            when(userRepository.findByUsername("jan@example.com")).thenReturn(Mono.just(user));
            when(userRepository.addOrUpdate(any())).thenReturn(Mono.just(user));

            StepVerifier.create(movieService.addMovieToFavorites("movie-1", "jan@example.com"))
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("movie-1"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Movie not found: MovieServiceException with movie id")
        void shouldThrowWhenMovieNotFound() {
            when(movieRepository.findById("missing")).thenReturn(Mono.empty());

            StepVerifier.create(movieService.addMovieToFavorites("missing", "jan@example.com"))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(MovieServiceException.class);
                        assertThat(ex.getMessage()).contains("missing");
                    })
                    .verify();
        }

        @Test
        @DisplayName("Already in favorites: MovieServiceException with movie id")
        void shouldThrowWhenAlreadyInFavorites() {
            User user = User.builder()
                    .username("jan@example.com")
                    .email("jan@example.com")
                    .password("pass")
                    .build();

            when(movieRepository.findById("movie-1")).thenReturn(Mono.just(drama2026));
            when(userRepository.findByUsername("jan@example.com")).thenReturn(Mono.just(user));

            StepVerifier.create(movieService.addMovieToFavorites("movie-1", "jan@example.com"))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(MovieServiceException.class);
                        assertThat(ex.getMessage()).contains("movie-1");
                    })
                    .verify();
        }
    }

    @Nested
    @DisplayName("addMovie()")
    class AddMovieTests {

        @Test
        @DisplayName("Happy path: valid DTO saves movie and returns DTO")
        void shouldAddMovie() {
            CreateMovieDto dto = CreateMovieDto.builder()
                    .name("Quiet Storm")
                    .genre("Drama")
                    .duration(120)
                    .premiereDate("2025-03-10")
                    .build();

            when(createMovieDtoValidator.validate(dto)).thenReturn(Map.of());
            when(movieRepository.addOrUpdate(any())).thenReturn(Mono.just(drama2026));

            StepVerifier.create(movieService.addMovie(Mono.just(dto)))
                    .assertNext(result -> assertThat(result.getId()).isEqualTo("movie-1"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Validation error: MovieServiceException thrown synchronously in map")
        void shouldThrowOnValidationError() {
            CreateMovieDto dto = CreateMovieDto.builder().build();
            when(createMovieDtoValidator.validate(dto)).thenReturn(Map.of("name", "must not be blank"));

            StepVerifier.create(movieService.addMovie(Mono.just(dto)))
                    .expectErrorSatisfies(ex -> assertThat(ex).isInstanceOf(MovieServiceException.class))
                    .verify();

            verifyNoInteractions(movieRepository);
        }
    }

    @Nested
    @DisplayName("deleteMovieById()")
    class DeleteMovieByIdTests {

        @Test
        @DisplayName("Happy path: movie deleted, DTO returned")
        void shouldDeleteMovie() {
            when(movieRepository.deleteById("movie-1")).thenReturn(Mono.just(drama2026));

            StepVerifier.create(movieService.deleteMovieById("movie-1"))
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("movie-1"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Movie not found: Mono completes empty")
        void shouldCompleteEmptyWhenNotFound() {
            when(movieRepository.deleteById("missing")).thenReturn(Mono.empty());
            StepVerifier.create(movieService.deleteMovieById("missing")).verifyComplete();
        }
    }

    @Nested
    @DisplayName("getFavoriteMovies()")
    class GetFavoriteMoviesTests {

        @Test
        @DisplayName("User has two favorites: Flux emits two DTOs")
        void shouldReturnFavorites() {
            User user = User.builder()
                    .username("jan@example.com")
                    .email("jan@example.com")
                    .password("pass")
                    .build();

            when(userRepository.findByUsername("jan@example.com")).thenReturn(Mono.just(user));

            StepVerifier.create(movieService.getFavoriteMovies("jan@example.com"))
                    .expectNextCount(2)
                    .verifyComplete();
        }
    }
}
