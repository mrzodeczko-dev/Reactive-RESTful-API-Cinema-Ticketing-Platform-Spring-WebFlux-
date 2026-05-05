package com.app.application.service;

import com.app.application.dto.AddCinemaToCityDto;
import com.app.application.dto.CityDto;
import com.app.application.dto.CreateCityDto;
import com.app.application.exception.CityServiceException;
import com.app.application.service.util.ServiceUtils;
import com.app.domain.cinema.Cinema;
import com.app.domain.cinema.CinemaRepository;
import com.app.domain.cinema_hall.CinemaHall;
import com.app.domain.cinema_hall.CinemaHallRepository;
import com.app.domain.city.City;
import com.app.domain.city.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final CinemaRepository cinemaRepository;
    private final CinemaHallRepository cinemaHallRepository;
    private final TransactionalOperator transactionalOperator;

    public Mono<CityDto> addCity(Mono<CreateCityDto> createCityDto) {
        return createCityDto
                .flatMap(dto -> cityRepository
                        .addOrUpdate(dto.toEntity()))
                .map(City::toDto);
    }

    public Mono<CityDto> findByName(String name) {
        return cityRepository.findByName(name)
                .switchIfEmpty(Mono.error(new CityServiceException("No city with name: %s".formatted(name))))
                .map(City::toDto);
    }

    public Mono<CityDto> addCinemaToCity(AddCinemaToCityDto addCinemaToCityDto) {

        return cinemaHallRepository.addOrUpdateMany(addCinemaToCityDto
                .getCinemaHallsCapacity().stream()
                .map(dtoVal -> CinemaHall.builder()
                        .positions(ServiceUtils.buildPositions(dtoVal.getRowNo(), dtoVal.getColNo()))
                        .movieEmissions(Collections.emptyList())
                        .build())
                .collect(Collectors.toList()))
                .collectList()
                .flatMap(cinemaHalls -> cinemaRepository.addOrUpdate(Cinema.builder()
                        .cinemaHalls(cinemaHalls)
                        .build()))
                .flatMap(cinema ->
                        cityRepository.findByName(addCinemaToCityDto.getCity())
                                .switchIfEmpty(Mono.error(() -> new CityServiceException("No city with name: %s".formatted(addCinemaToCityDto.getCity()))))
                                .flatMap(cityEntity ->
                                        cinemaRepository.addOrUpdate(cinema.setCity(cityEntity.getName()))
                                                .then(cityRepository.addOrUpdate(cityEntity.addCinema(cinema)))
                                                .map(City::toDto)
                                )
                ).as(transactionalOperator::transactional);

    }

    public Flux<CityDto> getAll() {
        return cityRepository.findAll().map(City::toDto);
    }
}
