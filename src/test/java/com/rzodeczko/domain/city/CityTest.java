package com.rzodeczko.domain.city;

import com.rzodeczko.domain.cinema.Cinema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("City")
class CityTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Creates City with all fields")
        void shouldCreateCityWithAllFields() {
            List<Cinema> cinemas = List.of(
                    Cinema.builder().id("c1").cityId("city-1").street("Main St").build(),
                    Cinema.builder().id("c2").cityId("city-1").street("Second St").build()
            );

            City city = new City("city-1", "Warsaw", cinemas);

            assertThat(city.id()).isEqualTo("city-1");
            assertThat(city.name()).isEqualTo("Warsaw");
            assertThat(city.cinemas()).hasSize(2);
        }

        @Test
        @DisplayName("Creates City with null cinemas")
        void shouldCreateCityWithNullCinemas() {
            City city = new City("city-1", "Warsaw", null);

            assertThat(city.id()).isEqualTo("city-1");
            assertThat(city.name()).isEqualTo("Warsaw");
            assertThat(city.cinemas()).isEmpty();
        }

        @Test
        @DisplayName("Creates City with empty cinemas list")
        void shouldCreateCityWithEmptyCinemasList() {
            City city = new City("city-1", "Warsaw", new ArrayList<>());

            assertThat(city.cinemas()).isEmpty();
        }

        @Test
        @DisplayName("No-arg constructor: creates City with nulls")
        void shouldCreateCityWithNulls() {
            City city = new City();

            assertThat(city.id()).isNull();
            assertThat(city.name()).isNull();
            assertThat(city.cinemas()).isEmpty();
        }

        @Test
        @DisplayName("Cinemas list is defensive copy")
        void shouldCreateDefensiveCopyOfCinemasList() {
            List<Cinema> originalCinemas = new ArrayList<>(List.of(
                    Cinema.builder().id("c1").cityId("city-1").street("Main St").build()
            ));
            City city = new City("city-1", "Warsaw", originalCinemas);

            originalCinemas.add(Cinema.builder().id("c2").cityId("city-1").street("Second St").build());

            assertThat(city.cinemas()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("Build with all fields: creates City")
        void shouldBuildCityWithAllFields() {
            List<Cinema> cinemas = List.of(
                    Cinema.builder().id("c1").cityId("city-1").street("Main St").build()
            );

            City city = City.builder()
                    .id("city-1")
                    .name("Warsaw")
                    .cinemas(cinemas)
                    .build();

            assertThat(city.id()).isEqualTo("city-1");
            assertThat(city.name()).isEqualTo("Warsaw");
            assertThat(city.cinemas()).hasSize(1);
        }

        @Test
        @DisplayName("Build without any fields: creates City with nulls")
        void shouldBuildCityWithNulls() {
            City city = City.builder().build();

            assertThat(city.id()).isNull();
            assertThat(city.name()).isNull();
            assertThat(city.cinemas()).isEmpty();
        }

        @Test
        @DisplayName("Build with partial fields: creates City")
        void shouldBuildCityWithPartialFields() {
            City city = City.builder()
                    .id("city-1")
                    .name("Warsaw")
                    .build();

            assertThat(city.id()).isEqualTo("city-1");
            assertThat(city.name()).isEqualTo("Warsaw");
            assertThat(city.cinemas()).isEmpty();
        }

        @Test
        @DisplayName("Builder is reusable: allows multiple builds")
        void shouldAllowMultipleBuilds() {
            var builder = City.builder()
                    .id("city-1")
                    .name("Warsaw");

            City city1 = builder.build();
            City city2 = builder
                    .id("city-2")
                    .build();

            assertThat(city1.id()).isEqualTo("city-1");
            assertThat(city1.name()).isEqualTo("Warsaw");

            assertThat(city2.id()).isEqualTo("city-2");
            assertThat(city2.name()).isEqualTo("Warsaw");
        }

        @Test
        @DisplayName("Builder with fluent API: allows chaining")
        void shouldSupportFluentChaining() {
            City city = City.builder()
                    .id("city-1")
                    .name("Warsaw")
                    .cinemas(new ArrayList<>())
                    .build();

            assertThat(city).isNotNull();
            assertThat(city.id()).isEqualTo("city-1");
        }
    }

    @Nested
    @DisplayName("Setters (wither pattern)")
    class SetterTests {

        @Test
        @DisplayName("setId: creates new City with new id")
        void shouldSetId() {
            City city = new City("city-1", "Warsaw", new ArrayList<>());

            City updated = city.withId("city-2");

            assertThat(updated.id()).isEqualTo("city-2");
            assertThat(updated.name()).isEqualTo("Warsaw");
            assertThat(city.id()).isEqualTo("city-1"); // Original unchanged
        }

        @Test
        @DisplayName("setName: creates new City with new name")
        void shouldSetName() {
            City city = new City("city-1", "Warsaw", new ArrayList<>());

            City updated = city.withName("Krakow");

            assertThat(updated.name()).isEqualTo("Krakow");
            assertThat(updated.id()).isEqualTo("city-1");
            assertThat(city.name()).isEqualTo("Warsaw"); // Original unchanged
        }

        @Test
        @DisplayName("setCinemas: creates new City with new cinemas")
        void shouldSetCinemas() {
            City city = new City("city-1", "Warsaw", new ArrayList<>());
            List<Cinema> newCinemas = List.of(
                    Cinema.builder().id("c1").city("Cinema 1").build()
            );

            City updated = city.withCinemas(newCinemas);

            assertThat(updated.cinemas()).hasSize(1);
            assertThat(city.cinemas()).isEmpty(); // Original unchanged
        }
    }

    @Nested
    @DisplayName("addCinema(Cinema)")
    class AddCinemaTests {

        @Test
        @DisplayName("Add cinema to empty list: adds cinema")
        void shouldAddCinemaToEmpty() {
            City city = new City("city-1", "Warsaw", new ArrayList<>());
            Cinema cinema = Cinema.builder().id("c1").cityId("city-1").street("Main St").build();

            City updated = city.addCinema(cinema);

            assertThat(updated.cinemas()).hasSize(1);
            assertThat(updated.cinemas().getFirst()).isEqualTo(cinema);
            assertThat(city.cinemas()).isEmpty(); // Original unchanged
        }

        @Test
        @DisplayName("Add cinema to existing list: adds cinema")
        void shouldAddCinemaToExisting() {
            Cinema cinema1 = Cinema.builder().id("c1").cityId("city-1").street("Main St").build();
            City city = new City("city-1", "Warsaw", new ArrayList<>(List.of(cinema1)));
            Cinema cinema2 = Cinema.builder().id("c2").cityId("city-1").street("Second St").build();

            City updated = city.addCinema(cinema2);

            assertThat(updated.cinemas()).hasSize(2);
            assertThat(updated.cinemas()).contains(cinema1, cinema2);
            assertThat(city.cinemas()).hasSize(1); // Original unchanged
        }

        @Test
        @DisplayName("Add cinema to null cinemas: creates list and adds cinema")
        void shouldAddCinemaToNull() {
            City city = new City("city-1", "Warsaw", null);
            Cinema cinema = Cinema.builder().id("c1").cityId("city-1").street("Main St").build();

            City updated = city.addCinema(cinema);

            assertThat(updated.cinemas()).hasSize(1);
            assertThat(updated.cinemas().getFirst()).isEqualTo(cinema);
        }
    }

    @Nested
    @DisplayName("GenericEntity")
    class GenericEntityTests {

        @Test
        @DisplayName("City implements GenericEntity")
        void shouldImplementGenericEntity() {
            City city = new City("city-1", "Warsaw", new ArrayList<>());

            assertThat(city).isInstanceOf(com.rzodeczko.domain.generic.GenericEntity.class);
        }
    }

    @Nested
    @DisplayName("Equality and Hashing")
    class EqualityTests {

        @Test
        @DisplayName("Same fields: are equal (record equality)")
        void shouldBeEqualWithSameFields() {
            List<Cinema> cinemas = new ArrayList<>();
            City city1 = new City("city-1", "Warsaw", cinemas);
            City city2 = new City("city-1", "Warsaw", cinemas);

            assertThat(city1).isEqualTo(city2);
        }

        @Test
        @DisplayName("Different ids: are not equal")
        void shouldNotBeEqualWithDifferentIds() {
            City city1 = new City("city-1", "Warsaw", new ArrayList<>());
            City city2 = new City("city-2", "Warsaw", new ArrayList<>());

            assertThat(city1).isNotEqualTo(city2);
        }

        @Test
        @DisplayName("Same hash code: for equal cities")
        void shouldHaveSameHashCodeWhenEqual() {
            List<Cinema> cinemas = new ArrayList<>();
            City city1 = new City("city-1", "Warsaw", cinemas);
            City city2 = new City("city-1", "Warsaw", cinemas);

            assertThat(city1.hashCode()).isEqualTo(city2.hashCode());
        }
    }
}

