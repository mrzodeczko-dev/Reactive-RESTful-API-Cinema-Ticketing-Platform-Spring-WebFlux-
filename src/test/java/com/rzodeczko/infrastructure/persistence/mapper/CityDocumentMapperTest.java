package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.city.City;
import com.rzodeczko.infrastructure.persistence.document.CinemaDocument;
import com.rzodeczko.infrastructure.persistence.document.CityDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CityDocumentMapperTest {

    @Nested
    @DisplayName("toDocument()")
    class ToDocumentTests {

        @Test
        @DisplayName("Null domain: null document")
        void shouldReturnNullWhenDomainIsNull() {
            assertThat(CityDocumentMapper.toDocument(null)).isNull();
        }

        @Test
        @DisplayName("City domain: cinemas and fields mapped")
        void shouldMapDomainToDocument() {
            CityDocument document = CityDocumentMapper.toDocument(city());

            assertThat(document.getId()).isEqualTo("city-1");
            assertThat(document.getName()).isEqualTo("Warsaw");
            assertThat(document.getCinemas()).hasSize(1);
            assertThat(document.getCinemas().getFirst().getId()).isEqualTo("cinema-1");
        }
    }

    @Nested
    @DisplayName("toDomain()")
    class ToDomainTests {

        @Test
        @DisplayName("Null document: null domain")
        void shouldReturnNullWhenDocumentIsNull() {
            assertThat(CityDocumentMapper.toDomain(null)).isNull();
        }

        @Test
        @DisplayName("City document: cinemas and fields mapped")
        void shouldMapDocumentToDomain() {
            City domain = CityDocumentMapper.toDomain(cityDocument());

            assertThat(domain.getId()).isEqualTo("city-1");
            assertThat(domain.getName()).isEqualTo("Warsaw");
            assertThat(domain.getCinemas()).hasSize(1);
            assertThat(domain.getCinemas().getFirst().getId()).isEqualTo("cinema-1");
        }
    }

    private City city() {
        return City.builder()
                .id("city-1")
                .name("Warsaw")
                .cinemas(List.of(Cinema.builder()
                        .id("cinema-1")
                        .cityId("city-1")
                        .street("Long Street")
                        .build()))
                .build();
    }

    private CityDocument cityDocument() {
        return new CityDocument(
                "city-1",
                "Warsaw",
                List.of(new CinemaDocument("cinema-1", "city-1", "Long Street", null))
        );
    }
}
