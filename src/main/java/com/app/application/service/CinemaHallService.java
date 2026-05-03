package com.app.application.service;

import com.app.application.dto.AddCinemaHallToCinemaDto;
import com.app.application.dto.CinemaHallDto;
import com.app.application.exception.CinemaHallServiceException;
import com.app.application.validator.AddCinemaHallToCinemaDtoValidator;
import com.app.application.validator.util.Validations;
import com.app.domain.cinema.CinemaRepository;
import com.app.domain.cinema_hall.CinemaHall;
import com.app.domain.cinema_hall.CinemaHallRepository;
import com.app.domain.vo.Position;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CinemaHallService {

    private final CinemaHallRepository cinemaHallRepository;
    private final CinemaRepository cinemaRepository;
    private final TransactionalOperator transactionalOperator;

    public Mono<CinemaHallDto> addCinemaHallToCinema(Mono<AddCinemaHallToCinemaDto> addCinemaHallToCinemaDtoMono) {

        return addCinemaHallToCinemaDtoMono
                .flatMap(dto -> {
                    // Validation — propagated as Mono.error to stay within the reactive pipeline
                    var errors = new AddCinemaHallToCinemaDtoValidator().validate(dto);
                    if (Validations.hasErrors(errors)) {
                        return Mono.error(new CinemaHallServiceException(Validations.createErrorMessage(errors)));
                    }
                    return Mono.just(dto);
                })
                .flatMap(dto -> cinemaRepository.findById(dto.getCinemaId())
                        // switchIfEmpty replaces the isNull-inside-map anti-pattern
                        .switchIfEmpty(Mono.error(new CinemaHallServiceException(
                                "No cinema with id: %s".formatted(dto.getCinemaId()))))
                        .flatMap(cinema -> cinemaHallRepository.addOrUpdate(CinemaHall.builder()
                                .cinemaId(cinema.getId())
                                .movieEmissions(new ArrayList<>())
                                .positions(createPositions(dto.getColNo(), dto.getRowNo()))
                                .build())
                                .flatMap(savedCinemaHall -> {
                                    cinema.getCinemaHalls().add(savedCinemaHall);
                                    return cinemaRepository.addOrUpdate(cinema)
                                            .thenReturn(savedCinemaHall);
                                })
                        )
                )
                .map(CinemaHall::toDto)
                .as(transactionalOperator::transactional);
    }

    private List<Position> createPositions(Integer colNo, Integer rowNo) {
        return IntStream.rangeClosed(1, colNo)
                .boxed()
                .collect(ArrayList::new, (list, col) -> IntStream
                                .rangeClosed(1, rowNo)
                                .boxed()
                                .map(row -> Position.builder()
                                        .colNo(col)
                                        .rowNo(row)
                                        .build())
                                .forEach(list::add),
                        List::addAll);
    }

    public Flux<CinemaHallDto> getAll() {
        return cinemaHallRepository
                .findAll()
                .map(CinemaHall::toDto);
    }

    public Flux<CinemaHallDto> getAllForCinema(String cinemaId) {
        return cinemaRepository.findById(cinemaId)
                .switchIfEmpty(Mono.error(() -> new CinemaHallServiceException(
                        "No cinema with id: %s".formatted(cinemaId))))
                .flatMapMany(cinema ->
                        cinemaHallRepository
                                .getAllForCinemaById(cinemaId)
                                .map(CinemaHall::toDto)
                );
    }
}
