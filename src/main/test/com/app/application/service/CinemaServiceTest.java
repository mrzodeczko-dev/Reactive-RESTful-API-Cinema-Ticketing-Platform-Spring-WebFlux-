package com.app.application.service;

import com.app.application.dto.CreateCinemaDto;
import com.app.application.dto.CreateCinemaHallDto;
import com.app.application.exception.CinemaServiceException;
import com.app.application.validator.CreateCinemaDtoValidator;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @Mock
    private CinemaRepository cinemaRepository;
    @Mock
    private CinemaHallRepository cinemaHallRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private CreateCinemaDtoValidator createCinemaDtoValidator;
    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private CinemaService cinemaService;

    private Cinema sampleCinema;
    private City sampleCity;
    private CinemaHall sampleHall;

    @BeforeEach
    void setUp() {
        sampleHall = CinemaHall.builder()
                .id("hall-1")
                .positions(List.of())
                .movieEmissions(List.of())
                .build();

        sampleCinema = Cinema.builder()
                .id("cinema-1")
                .street("ul. Marszalkowska 1")
                .city("Warsaw")
                .cinemaHalls(new ArrayList<>(List.of(sampleHall)))
                .build();

        sampleCity = City.builder()
                .id("city-1")
                .name("Warsaw")
                .cinemas(new ArrayList<>(List.of(sampleCinema)))
                .build();
    }

    // -------------------------------------------------------------------------
    // addCinema
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addCinema — should return CinemaDto on success")
    void addCinema_shouldReturnCinemaDto() {
        var dto = CreateCinemaDto.builder()
                .city("Warsaw")
                .street("ul. Marszalkowska 1")
                .cinemaHallsCapacity(List.of(
                        CreateCinemaHallDto.builder().rowNo(5).colNo(10).build()
                ))
                .build();

        when(createCinemaDtoValidator.validate(dto)).thenReturn(Map.of());
        when(cinemaHallRepository.addOrUpdateMany(anyList())).thenReturn(Flux.just(sampleHall));
        when(cinemaRepository.addOrUpdate(any(Cinema.class))).thenReturn(Mono.just(sampleCinema));
        when(cityRepository.findByName("Warsaw")).thenReturn(Mono.just(sampleCity));
        when(cityRepository.addOrUpdate(any(City.class))).thenReturn(Mono.just(sampleCity));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StepVerifier.create(cinemaService.addCinema(dto))
                .assertNext(result -> assertThat(result.getCity()).isEqualTo("Warsaw"))
                .verifyComplete();
    }

    @Test
    @DisplayName("addCinema — should throw CinemaServiceException when validation fails")
    void addCinema_whenValidationFails_shouldThrowException() {
        var dto = CreateCinemaDto.builder().build();
        when(createCinemaDtoValidator.validate(dto))
                .thenReturn(Map.of("city", "City is required"));

        StepVerifier.create(cinemaService.addCinema(dto))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(CinemaServiceException.class);
                    assertThat(ex.getMessage()).contains("city");
                })
                .verify();

        verifyNoInteractions(cinemaRepository, cityRepository, cinemaHallRepository);
    }

    @Test
    @DisplayName("addCinema — should throw CinemaServiceException when city not found")
    void addCinema_whenCityNotFound_shouldThrowException() {
        var dto = CreateCinemaDto.builder()
                .city("NonExistent")
                .street("ul. Testowa 1")
                .cinemaHallsCapacity(List.of(
                        CreateCinemaHallDto.builder().rowNo(3).colNo(5).build()
                ))
                .build();

        when(createCinemaDtoValidator.validate(dto)).thenReturn(Map.of());
        when(cinemaHallRepository.addOrUpdateMany(anyList())).thenReturn(Flux.just(sampleHall));
        when(cinemaRepository.addOrUpdate(any(Cinema.class))).thenReturn(Mono.just(sampleCinema));
        when(cityRepository.findByName("NonExistent")).thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StepVerifier.create(cinemaService.addCinema(dto))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(CinemaServiceException.class);
                    assertThat(ex.getMessage()).contains("NonExistent");
                })
                .verify();
    }

    // -------------------------------------------------------------------------
    // getAll
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAll — should return all cinemas")
    void getAll_shouldReturnAllCinemas() {
        Cinema cinema2 = Cinema.builder().id("cinema-2").city("Krakow").cinemaHalls(List.of()).build();
        when(cinemaRepository.findAll()).thenReturn(Flux.just(sampleCinema, cinema2));

        StepVerifier.create(cinemaService.getAll())
                .assertNext(dto -> assertThat(dto.getCity()).isEqualTo("Warsaw"))
                .assertNext(dto -> assertThat(dto.getCity()).isEqualTo("Krakow"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getAll — should return empty flux when no cinemas exist")
    void getAll_whenNoCinemas_shouldReturnEmpty() {
        when(cinemaRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(cinemaService.getAll())
                .verifyComplete();
    }

    // -------------------------------------------------------------------------
    // getAllByCity
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAllByCity — should filter cinemas by city name")
    void getAllByCity_shouldReturnCinemasByCity() {
        when(cinemaRepository.findAllByCity("Warsaw")).thenReturn(Flux.just(sampleCinema));

        StepVerifier.create(cinemaService.getAllByCity("Warsaw"))
                .assertNext(dto -> assertThat(dto.getCity()).isEqualTo("Warsaw"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getAllByCity — should return empty flux when no cinemas in given city")
    void getAllByCity_whenNoCinemasInCity_shouldReturnEmpty() {
        when(cinemaRepository.findAllByCity("Gdansk")).thenReturn(Flux.empty());

        StepVerifier.create(cinemaService.getAllByCity("Gdansk"))
                .verifyComplete();
    }

    // -------------------------------------------------------------------------
    // addCinemaHallToCinema
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addCinemaHallToCinema — should add hall and return updated CinemaDto")
    void addCinemaHallToCinema_shouldAddHallSuccessfully() {
        var hallDto = CreateCinemaHallDto.builder().rowNo(5).colNo(8).build();
        var newHall = CinemaHall.builder().id("hall-2").positions(List.of()).movieEmissions(List.of()).build();

        when(cinemaRepository.findById("cinema-1")).thenReturn(Mono.just(sampleCinema));
        when(cinemaHallRepository.addOrUpdate(any(CinemaHall.class))).thenReturn(Mono.just(newHall));
        when(cinemaRepository.addOrUpdate(any(Cinema.class))).thenReturn(Mono.just(sampleCinema));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StepVerifier.create(cinemaService.addCinemaHallToCinema("cinema-1", hallDto))
                .assertNext(dto -> assertThat(dto).isNotNull())
                .verifyComplete();
    }

    @Test
    @DisplayName("addCinemaHallToCinema — should throw exception when cinema not found")
    void addCinemaHallToCinema_whenCinemaNotFound_shouldThrowException() {
        var hallDto = CreateCinemaHallDto.builder().rowNo(5).colNo(8).build();

        when(cinemaRepository.findById("missing-id")).thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StepVerifier.create(cinemaService.addCinemaHallToCinema("missing-id", hallDto))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(CinemaServiceException.class);
                    assertThat(ex.getMessage()).contains("missing-id");
                })
                .verify();
    }
}