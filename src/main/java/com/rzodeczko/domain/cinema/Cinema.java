package com.rzodeczko.domain.cinema;

import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.generic.GenericEntity;

import java.util.ArrayList;
import java.util.List;

public record Cinema(
        String id,
        String cityName,
        String street,
        List<CinemaHall> cinemaHalls
) implements GenericEntity {

    public Cinema {
        cinemaHalls = cinemaHalls == null ? new ArrayList<>() : new ArrayList<>(cinemaHalls);
    }

    public Cinema() {
        this(null, null, null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Cinema withId(String id) {
        return new Cinema(id, cityName, street, cinemaHalls);
    }

    public Cinema withCityId(String cityId) {
        return new Cinema(id, cityId, street, cinemaHalls);
    }

    public Cinema withStreet(String street) {
        return new Cinema(id, cityName, street, cinemaHalls);
    }

    public Cinema withCinemaHalls(List<CinemaHall> cinemaHalls) {
        return new Cinema(id, cityName, street, cinemaHalls);
    }

    public Cinema withCinemasIdForCinemaHalls(String cinemaId) {
        if (cinemaHalls == null) {
            return this;
        }
        var updatedCinemaHalls = cinemaHalls.stream()
                .map(cinemaHall -> cinemaHall.withCinemaId(cinemaId))
                .toList();
        return withCinemaHalls(updatedCinemaHalls);
    }

    public Cinema addCinemaHall(CinemaHall cinemaHall) {
        var updatedCinemaHalls = new ArrayList<>(cinemaHalls);
        updatedCinemaHalls.add(cinemaHall);
        return withCinemaHalls(updatedCinemaHalls);
    }

    public static class Builder {
        private String id;
        private String cityId;
        private String street;
        private List<CinemaHall> cinemaHalls;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder city(String city) {
            this.cityId = city;
            return this;
        }

        public Builder cityId(String cityId) {
            this.cityId = cityId;
            return this;
        }

        public Builder street(String street) {
            this.street = street;
            return this;
        }

        public Builder cinemaHalls(List<CinemaHall> cinemaHalls) {
            this.cinemaHalls = cinemaHalls;
            return this;
        }

        public Cinema build() {
            return new Cinema(id, cityId, street, cinemaHalls);
        }
    }
}
