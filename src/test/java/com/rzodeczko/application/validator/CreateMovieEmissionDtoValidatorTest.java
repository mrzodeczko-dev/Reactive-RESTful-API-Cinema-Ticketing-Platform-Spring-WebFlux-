package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.CreateMovieEmissionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CreateMovieEmissionDtoValidatorTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CreateMovieEmissionDtoValidator validator = new CreateMovieEmissionDtoValidator();

    @Nested
    @DisplayName("validate()")
    class ValidateTests {

        @Test
        @DisplayName("Valid emission: no errors")
        void shouldReturnNoErrorsForValidEmission() {
            assertThat(validator.validate(validDto())).isEmpty();
        }

        @Test
        @DisplayName("Null DTO: dto object error only")
        void shouldReturnErrorWhenDtoIsNull() {
            assertThat(validator.validate(null))
                    .containsExactly(Map.entry("dto object", "is null"));
        }

        @Test
        @DisplayName("Missing identifiers: movie and cinema hall errors")
        void shouldReturnErrorsWhenIdentifiersAreMissing() {
            CreateMovieEmissionDto dto = CreateMovieEmissionDto.builder()
                    .movieId(null)
                    .cinemaHallId(null)
                    .startTime(futureDateTime())
                    .baseTicketPrice("35.00")
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("movieId", "is null")
                    .containsEntry("cinemaHallId", "is null");
        }

        @Test
        @DisplayName("Invalid start time format: start time error")
        void shouldReturnErrorWhenStartTimeHasInvalidFormat() {
            CreateMovieEmissionDto dto = CreateMovieEmissionDto.builder()
                    .movieId("movie-1")
                    .cinemaHallId("hall-1")
                    .startTime("16-05-2027 20:00")
                    .baseTicketPrice("35.00")
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("start time: 16-05-2027 20:00", "is not valid. Valid format is: yyyy-MM-dd HH:mm");
        }

        @Test
        @DisplayName("Past start time: start time error")
        void shouldReturnErrorWhenStartTimeIsInPast() {
            CreateMovieEmissionDto dto = CreateMovieEmissionDto.builder()
                    .movieId("movie-1")
                    .cinemaHallId("hall-1")
                    .startTime("2020-01-01 20:00")
                    .baseTicketPrice("35.00")
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("start time: 2020-01-01 20:00", "is not valid. Valid format is: yyyy-MM-dd HH:mm");
        }

        @Test
        @DisplayName("Ticket price accepts whole amount and amount with two decimals")
        void shouldAcceptValidTicketPriceFormats() {
            assertThat(validator.validate(validDtoWithBaseTicketPrice("35"))).isEmpty();
            assertThat(validator.validate(validDtoWithBaseTicketPrice("35.00"))).isEmpty();
        }

        @Test
        @DisplayName("Invalid ticket price: price format error")
        void shouldReturnErrorWhenTicketPriceFormatIsInvalid() {
            CreateMovieEmissionDto dto = validDtoWithBaseTicketPrice("35.5");

            assertThat(validator.validate(dto))
                    .containsEntry("base ticket price: 35.5", "should have valid format \\d+(\\.\\d{2})?");
        }
    }

    private CreateMovieEmissionDto validDto() {
        return validDtoWithBaseTicketPrice("35.00");
    }

    private CreateMovieEmissionDto validDtoWithBaseTicketPrice(String baseTicketPrice) {
        return CreateMovieEmissionDto.builder()
                .movieId("movie-1")
                .cinemaHallId("hall-1")
                .startTime(futureDateTime())
                .baseTicketPrice(baseTicketPrice)
                .build();
    }

    private String futureDateTime() {
        return LocalDateTime.now().plusYears(1).withSecond(0).withNano(0).format(DATE_TIME_FORMATTER);
    }
}
