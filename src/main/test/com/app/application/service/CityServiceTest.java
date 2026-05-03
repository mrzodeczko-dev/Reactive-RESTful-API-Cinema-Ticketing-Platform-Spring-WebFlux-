package com.app.application.service;

import com.app.application.dto.AddCinemaToCityDto;
import com.app.application.dto.CityDto;
import com.app.application.dto.CreateCinemaHallDto;
import com.app.application.dto.CreateCityDto;
import com.app.application.exception.CityServiceException;
import com.app.domain.cinema.Cinema;
import com.app.domain.cinema.CinemaRepository;
import com.app.domain.cinema_hall.CinemaHall;
import com.app.domain.cinema_hall.CinemaHallRepository;
import com.app.domain.city.City;
import com.app.domain.city.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private CinemaHallRepository cinemaHallRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private CityService cityService;

    private City sampleCity;
    private CityDto sampleCityDto;

    @BeforeEach
    void setUp() {
        sampleCity = City.builder()
                .id("city-1")
                .name("Warsaw")
                .cinemas(new ArrayList<>())
                .build();
        sampleCityDto = CityDto.builder()
                .id("city-1")
                .name("Warsaw")
                .build();
    }

    // -------------------------------------------------------------------------
    // addCity
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addCity — should save city and return DTO")
    void addCity_shouldSaveAndReturnDto() {
        var dto = CreateCityDto.builder().name("Warsaw").build();

        when(cityRepository.addOrUpdate(any(City.class))).thenReturn(Mono.just(sampleCity));

        StepVerifier.create(cityService.addCity(Mono.just(dto)))
                .assertNext(result -> {
                    assertThat(result.getId()).isEqualTo("city-1");
                    assertThat(result.getName()).isEqualTo("Warsaw");
                })
                .verifyComplete();

        verify(cityRepository, times(1)).addOrUpdate(any(City.class));
    }

    // -------------------------------------------------------------------------
    // findByName
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByName — should return CityDto when city exists")
    void findByName_whenCityExists_shouldReturnDto() {
        when(cityRepository.findByName("Warsaw")).thenReturn(Mono.just(sampleCity));

        StepVerifier.create(cityService.findByName("Warsaw"))
                .assertNext(dto -> assertThat(dto.getName()).isEqualTo("Warsaw"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findByName — should throw CityServiceException when city not found")
    void findByName_whenCityNotFound_shouldThrowException() {
        when(cityRepository.findByName("Unknown")).thenReturn(Mono.empty());

        StepVerifier.create(cityService.findByName("Unknown"))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(CityServiceException.class);
                    assertThat(ex.getMessage()).contains("Unknown");
                })
                .verify();
    }

    // -------------------------------------------------------------------------
    // getAll
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAll — should return all cities as DTOs")
    void getAll_shouldReturnAllCities() {
        City city2 = City.builder().id("city-2").name("Krakow").cinemas(List.of()).build();

        when(cityRepository.findAll()).thenReturn(Flux.just(sampleCity, city2));

        StepVerifier.create(cityService.getAll())
                .assertNext(dto -> assertThat(dto.getName()).isEqualTo("Warsaw"))
                .assertNext(dto -> assertThat(dto.getName()).isEqualTo("Krakow"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getAll — should return empty flux when no cities")
    void getAll_whenNoCities_shouldReturnEmptyFlux() {
        when(cityRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(cityService.getAll())
                .verifyComplete();
    }

    @Test
    @DisplayName("addCinemaToCity — should save cinema in city and return updated CityDto")
    void addCinemaToCity_shouldReturnUpdatedCityDto() {
        var createCinemaHallDto = CreateCinemaHallDto.builder()
                .rowNo(5)
                .colNo(10)
                .build();

        var dto = AddCinemaToCityDto.builder()
                .city("Warsaw")
                .cinemaHallsCapacity(new ArrayList<>(List.of(createCinemaHallDto)))
                .build();

        var hall = CinemaHall.builder()
                .id("hall-1")
                .positions(List.of())
                .movieEmissions(Collections.emptyList())
                .build();

        var cinema = Cinema.builder()
                .id("cinema-1")
                .cinemaHalls(new ArrayList<>(List.of(hall)))
                .build();

        when(cinemaHallRepository.addOrUpdateMany(anyList())).thenReturn(Flux.just(hall));
        when(cityRepository.findByName("Warsaw")).thenReturn(Mono.just(sampleCity));
        when(cinemaRepository.addOrUpdate(any(Cinema.class))).thenReturn(Mono.just(cinema));
        when(cityRepository.addOrUpdate(any(City.class))).thenReturn(Mono.just(sampleCity));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        StepVerifier.create(cityService.addCinemaToCity(dto))
                .assertNext(result -> assertThat(result.getName()).isEqualTo("Warsaw"))
                .verifyComplete();

        verify(cityRepository, times(1)).addOrUpdate(any(City.class));
        verify(transactionalOperator, times(1)).transactional(any(Mono.class));

    }

    @Test
    @DisplayName("addCinemaToCity — should throw CityServiceException when city not found")
    void addCinemaToCity_whenCityNotFound_shouldThrowException() {
        var dto = AddCinemaToCityDto.builder()
                .city("NonExistent")
                .cinemaHallsCapacity(List.of(
                        CreateCinemaHallDto.builder().rowNo(3).colNo(5).build()
                ))
                .build();

        var hall = CinemaHall.builder().id("hall-1").positions(List.of()).movieEmissions(Collections.emptyList()).build();
        var cinema = Cinema.builder().id("cinema-1").cinemaHalls(new ArrayList<>(List.of(hall))).build();

        when(cinemaHallRepository.addOrUpdateMany(anyList())).thenReturn(Flux.just(hall));
        when(cinemaRepository.addOrUpdate(any(Cinema.class))).thenReturn(Mono.just(cinema));
        when(cityRepository.findByName("NonExistent")).thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StepVerifier.create(cityService.addCinemaToCity(dto))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(CityServiceException.class);
                    assertThat(ex.getMessage()).contains("NonExistent");
                })
                .verify();

        verify(transactionalOperator, times(1)).transactional(any(Mono.class));
    }

}