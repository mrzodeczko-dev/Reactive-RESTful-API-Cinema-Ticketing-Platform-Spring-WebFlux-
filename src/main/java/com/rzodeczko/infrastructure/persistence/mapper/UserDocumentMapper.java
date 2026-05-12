package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.user.User;
import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.infrastructure.persistence.document.MovieDocument;
import com.rzodeczko.infrastructure.persistence.document.UserDocument;

import java.util.List;
import java.util.stream.Collectors;

public final class UserDocumentMapper {

    private UserDocumentMapper() {
    }

    public static UserDocument toDocument(User u) {
        if (u == null) return null;
        UserDocument doc = new UserDocument();
        doc.setId(u.getId());
        doc.setUsername(u.getUsername());
        doc.setPassword(u.getPassword());
        doc.setRole(u.getRole());
        doc.setBirthDate(u.getBirthDate());
        doc.setEmail(u.getEmail());
        doc.setFavoriteMovies(toDocs(u.getFavoriteMovies()));
        return doc;
    }

    public static User toDomain(UserDocument doc) {
        if (doc == null) {
            return null;
        }
        User u = User.builder()
                .id(doc.getId())
                .username(doc.getUsername())
                .password(doc.getPassword())
                .email(doc.getEmail())
                .birthDate(doc.getBirthDate())
                .role(doc.getRole() != null ? doc.getRole() : Role.ROLE_USER)
                .build();
        if (doc.getFavoriteMovies() != null) {
            for (Movie m : toDomainMovies(doc.getFavoriteMovies())) {
                u = u.addMovieToFavorites(m);
            }
        }
        return u;
    }

    private static List<MovieDocument> toDocs(List<Movie> movies) {
        if (movies == null) {
            return null;
        }
        return movies.stream().map(MovieDocumentMapper::toDocument).collect(Collectors.toList());
    }

    private static List<Movie> toDomainMovies(List<MovieDocument> docs) {
        if (docs == null) {
            return null;
        }
        return docs.stream().map(MovieDocumentMapper::toDomain).collect(Collectors.toList());
    }
}
