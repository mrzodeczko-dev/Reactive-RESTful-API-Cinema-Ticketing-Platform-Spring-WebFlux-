package com.app.application.dto;

import com.app.domain.security.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserDto {

    private String username;
    private String password;
    private String passwordConfirmation;
    private String birthDate;
    private String email;

    public User toEntity() {
        return User.builder()
                .password(password)
                .birthDate(LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .username(username)
                .email(email)
                .build();

    }

    public CreateUserDto setPassword(String password) {
        this.password = password;
        return this;
    }
}