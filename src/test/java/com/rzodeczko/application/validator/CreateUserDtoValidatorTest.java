package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.CreateUserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CreateUserDtoValidatorTest {

    private final CreateUserDtoValidator validator = new CreateUserDtoValidator();

    @Nested
    @DisplayName("validate()")
    class ValidateTests {

        @Test
        @DisplayName("Valid user: no errors")
        void shouldReturnNoErrorsForValidUser() {
            assertThat(validator.validate(validDto())).isEmpty();
        }

        @Test
        @DisplayName("Null DTO: dto object error only")
        void shouldReturnErrorWhenDtoIsNull() {
            assertThat(validator.validate(null))
                    .containsExactly(Map.entry("dto object", "is null"));
        }

        @Test
        @DisplayName("Invalid birth date format: birth date format error")
        void shouldReturnErrorWhenBirthDateHasInvalidFormat() {
            CreateUserDto dto = CreateUserDto.builder()
                    .username("janek")
                    .password("secret")
                    .passwordConfirmation("secret")
                    .birthDate("1990-01-01")
                    .email("jan@example.com")
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("Birth date 1990-01-01:", "Birth date format should be: dd-MM-yyyy");
        }

        @Test
        @DisplayName("Underage user: adult validation error")
        void shouldReturnErrorWhenUserIsUnderage() {
            CreateUserDto dto = CreateUserDto.builder()
                    .username("janek")
                    .password("secret")
                    .passwordConfirmation("secret")
                    .birthDate("01-01-2020")
                    .email("jan@example.com")
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("Birt date", "User has to be an adult");
        }

        @Test
        @DisplayName("Password mismatch: confirmation error")
        void shouldReturnErrorWhenPasswordConfirmationDoesNotMatch() {
            CreateUserDto dto = CreateUserDto.builder()
                    .username("janek")
                    .password("secret")
                    .passwordConfirmation("different")
                    .birthDate("01-01-1990")
                    .email("jan@example.com")
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("Password confirmation", "Password and confirmation password does not match");
        }

        @Test
        @DisplayName("Invalid email and short username: both errors returned")
        void shouldReturnAllIndependentErrors() {
            CreateUserDto dto = CreateUserDto.builder()
                    .username("jan")
                    .password("secret")
                    .passwordConfirmation("secret")
                    .birthDate("01-01-1990")
                    .email("not-an-email")
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("Username", "Minimum length of user is 5 characters")
                    .containsEntry("email", "is not valid");
        }
    }

    private CreateUserDto validDto() {
        return CreateUserDto.builder()
                .username("janek")
                .password("secret")
                .passwordConfirmation("secret")
                .birthDate("01-01-1990")
                .email("jan@example.com")
                .build();
    }
}
