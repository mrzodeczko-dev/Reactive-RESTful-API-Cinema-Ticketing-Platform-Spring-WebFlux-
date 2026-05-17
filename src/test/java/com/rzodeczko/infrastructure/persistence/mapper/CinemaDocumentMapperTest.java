package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.vo.Position;
import com.rzodeczko.infrastructure.persistence.document.CinemaDocument;
import com.rzodeczko.infrastructure.persistence.document.CinemaHallDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CinemaDocumentMapperTest {

    @Nested
    @DisplayName("toDocument()")
    class ToDocumentTests {

        @Test
        @DisplayName("Null domain: null document")
        void shouldReturnNullWhenDomainIsNull() {
            assertThat(CinemaDocumentMapper.toDocument(null)).isNull();
        }

        @Test
        @DisplayName("Cinema domain: halls and fields mapped")
        void shouldMapDomainToDocument() {
            CinemaDocument document = CinemaDocumentMapper.toDocument(cinema("cinema-1"));

            assertThat(document.getId()).isEqualTo("cinema-1");
            assertThat(document.getCityName()).isEqualTo("city-1");
            assertThat(document.getStreet()).isEqualTo("Long Street");
            assertThat(document.getCinemaHalls()).hasSize(1);
            assertThat(document.getCinemaHalls().getFirst().getId()).isEqualTo("hall-1");
        }

        @Test
        @DisplayName("Cinema list: documents preserve order")
        void shouldMapDomainListToDocuments() {
            List<CinemaDocument> documents = CinemaDocumentMapper.toDocuments(List.of(
                    cinema("cinema-1"),
                    cinema("cinema-2")
            ));

            assertThat(documents).extracting(CinemaDocument::getId)
                    .containsExactly("cinema-1", "cinema-2");
        }
    }

    @Nested
    @DisplayName("toDomain()")
    class ToDomainTests {

        @Test
        @DisplayName("Null document: null domain")
        void shouldReturnNullWhenDocumentIsNull() {
            assertThat(CinemaDocumentMapper.toDomain(null)).isNull();
        }

        @Test
        @DisplayName("Cinema document: halls and fields mapped")
        void shouldMapDocumentToDomain() {
            Cinema domain = CinemaDocumentMapper.toDomain(cinemaDocument("cinema-1"));

            assertThat(domain.id()).isEqualTo("cinema-1");
            assertThat(domain.cityName()).isEqualTo("city-1");
            assertThat(domain.street()).isEqualTo("Long Street");
            assertThat(domain.cinemaHalls()).hasSize(1);
            assertThat(domain.cinemaHalls().getFirst().id()).isEqualTo("hall-1");
        }

        @Test
        @DisplayName("Null list: null domains list")
        void shouldReturnNullDomainsWhenListIsNull() {
            assertThat(CinemaDocumentMapper.toDomains(null)).isNull();
        }
    }

    private Cinema cinema(String id) {
        return Cinema.builder()
                .id(id)
                .cityId("city-1")
                .street("Long Street")
                .cinemaHalls(List.of(CinemaHall.builder()
                        .id("hall-1")
                        .positions(List.of(new Position(1, 1)))
                        .build()))
                .build();
    }

    private CinemaDocument cinemaDocument(String id) {
        return new CinemaDocument(
                id,
                "city-1",
                "Long Street",
                List.of(new CinemaHallDocument("hall-1", List.of(new Position(1, 1)), id, null))
        );
    }
}
