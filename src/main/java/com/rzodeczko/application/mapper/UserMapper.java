package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.UserDto;
import com.rzodeczko.domain.user.User;

import java.util.Collections;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.id())
                .username(user.username())
                .role(user.role() != null ? user.role().name() : null)
                .birthDate(nonNull(user.birthDate()) ? user.birthDate().toString() : null)
                .favoriteMovies(isNull(user.favoriteMovies()) ? Collections.emptyList() :
                        user.favoriteMovies()
                                .stream()
                                .map(MovieMapper::toDto)
                                .collect(Collectors.toList()))
                .email(user.email())
                .build();
    }
}