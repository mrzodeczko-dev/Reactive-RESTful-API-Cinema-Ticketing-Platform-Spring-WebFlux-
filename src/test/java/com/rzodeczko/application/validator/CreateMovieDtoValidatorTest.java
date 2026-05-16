package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.CreateMovieDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CreateMovieDtoValidatorTest {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final CreateMovieDtoValidator validator = new CreateMovieDtoValidator();

    @Nested
    @DisplayName("validate()")
    class ValidateTests {

        @Test
        @DisplayName("Valid movie: no errors")
        void shouldReturnNoErrorsForValidMovie() {
            assertThat(validator.validate(validDto())).isEmpty();
        }

        @Test
        @DisplayName("Null DTO: dto object error only")
        void shouldReturnErrorWhenDtoIsNull() {
            assertThat(validator.validate(null))
                    .containsExactly(Map.entry("dto object", "is null"));
        }

        @Test
        @DisplayName("Invalid genre: genre error")
        void shouldReturnErrorWhenGenreIsInvalid() {
            CreateMovieDto dto = CreateMovieDto.builder()
                    .genre("Western")
                    .name("Quiet Storm")
                    .duration(120)
                    .premiereDate(futureDate())
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("genre Western", "is not valid");
        }

        @Test
        @DisplayName("Duration outside range: duration error")
        void shouldReturnErrorWhenDurationIsOutsideAllowedRange() {
            CreateMovieDto dto = CreateMovieDto.builder()
                    .genre("Drama")
                    .name("Quiet Storm")
                    .duration(301)
                    .premiereDate(futureDate())
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("duration 301", "is not valid");
        }

        @Test
        @DisplayName("Past premiere date: premiere date error")
        void shouldReturnErrorWhenPremiereDateIsNotFutureDate() {
            CreateMovieDto dto = CreateMovieDto.builder()
                    .genre("Drama")
                    .name("Quiet Storm")
                    .duration(120)
                    .premiereDate("01-01-2020")
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("premiere date 01-01-2020", "is not valid");
        }

        @Test
        @DisplayName("Multiple invalid fields: all errors returned")
        void shouldReturnAllIndependentErrors() {
            CreateMovieDto dto = CreateMovieDto.builder()
                    .genre(null)
                    .name("Q")
                    .duration(10)
                    .premiereDate("bad-date")
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("genre null", "is not valid")
                    .containsEntry("name Q", "is not valid")
                    .containsEntry("duration 10", "is not valid")
                    .containsEntry("premiere date bad-date", "is not valid");
        }
    }

    private CreateMovieDto validDto() {
        return CreateMovieDto.builder()
                .genre("Drama")
                .name("Quiet Storm")
                .duration(120)
                .premiereDate(futureDate())
                .build();
    }

    private String futureDate() {
        return LocalDate.now().plusYears(1).format(DATE_FORMATTER);
    }
}
