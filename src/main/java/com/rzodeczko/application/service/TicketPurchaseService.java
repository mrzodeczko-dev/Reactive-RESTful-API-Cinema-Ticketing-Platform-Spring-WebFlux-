package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateTicketPurchaseDto;
import com.rzodeczko.application.dto.TicketDetailsDto;
import com.rzodeczko.application.dto.TicketPurchaseDto;
import com.rzodeczko.application.exception.TicketOrderServiceException;
import com.rzodeczko.application.exception.TicketPurchaseServiceException;
import com.rzodeczko.application.mapper.TicketPurchaseMapper;
import com.rzodeczko.application.port.out.*;
import com.rzodeczko.application.validator.CreateTicketPurchaseDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.domain.ticket.enums.TicketStatus;
import com.rzodeczko.domain.ticket_order.TicketOrder;
import com.rzodeczko.domain.ticket_order.enums.TicketOrderStatus;
import com.rzodeczko.domain.ticket_purchase.TicketPurchase;
import com.rzodeczko.domain.vo.Discount;
import com.rzodeczko.domain.vo.Money;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class TicketPurchaseService {

    private final TicketPurchasePort ticketPurchasePort;
    private final CreateTicketPurchaseDtoValidator createTicketPurchaseDtoValidator;
    private final MovieEmissionPort movieEmissionPort;
    private final MoviePort moviePort;
    private final UserPort userPort;
    private final CinemaHallPort cinemaHallPort;
    private final TicketPort ticketPort;
    private final CityPort cityPort;
    private final TicketOrderPort ticketOrderPort;
    private final CinemaPort cinemaPort;
    private final TransactionPort transactionPort;

    public TicketPurchaseService(TicketPurchasePort ticketPurchasePort,
                                 CreateTicketPurchaseDtoValidator createTicketPurchaseDtoValidator,
                                 MovieEmissionPort movieEmissionPort,
                                 MoviePort moviePort, UserPort userPort,
                                 CinemaHallPort cinemaHallPort, TicketPort ticketPort,
                                 CityPort cityPort, TicketOrderPort ticketOrderPort,
                                 CinemaPort cinemaPort, TransactionPort transactionPort) {
        this.ticketPurchasePort = ticketPurchasePort;
        this.createTicketPurchaseDtoValidator = createTicketPurchaseDtoValidator;
        this.movieEmissionPort = movieEmissionPort;
        this.moviePort = moviePort;
        this.userPort = userPort;
        this.cinemaHallPort = cinemaHallPort;
        this.ticketPort = ticketPort;
        this.cityPort = cityPort;
        this.ticketOrderPort = ticketOrderPort;
        this.cinemaPort = cinemaPort;
        this.transactionPort = transactionPort;
    }

    public Mono<TicketPurchaseDto> purchaseTicket(Mono<? extends Principal> principal, CreateTicketPurchaseDto createPurchaseDto) {

        var errors = createTicketPurchaseDtoValidator.validate(createPurchaseDto);
        if (Validations.hasErrors(errors)) {
            return Mono.error(() -> new TicketPurchaseServiceException(
                    "Validation errors: %s".formatted(Validations.createErrorMessage(errors))));
        }

        Mono<TicketPurchase> result = movieEmissionPort
                .findById(createPurchaseDto.movieEmissionId())
                .switchIfEmpty(Mono.error(() -> new TicketOrderServiceException(
                        "No movie emission with id: %s".formatted(createPurchaseDto.movieEmissionId()))))
                .flatMap(movieEmission ->
                        createPurchaseDto.areAllPositionsAvailable(movieEmission.getFreePositions())
                                ? Mono.just(movieEmission)
                                : Mono.error(() -> new TicketOrderServiceException("Positions are not available")))
                .flatMap(movieEmission -> Mono.zip(
                        movieEmissionPort.addOrUpdate(movieEmission.removeFreePositions(
                                createPurchaseDto.ticketsDetails().stream()
                                        .map(TicketDetailsDto::position)
                                        .collect(Collectors.toList()))),
                        principal.flatMap(p -> userPort.findByUsername(p.getName()))
                ).map(tuple -> TicketPurchase.builder()
                        .purchaseDate(LocalDate.now())
                        .ticketGroupType(createPurchaseDto.ticketGroupType())
                        .user(tuple.getT2())
                        .movieEmission(tuple.getT1())
                        .tickets(createPurchaseDto.ticketsDetails()
                                .stream()
                                .map(ticketDetailsDto -> {
                                    Discount totalDiscount = createPurchaseDto.getBaseDiscount()
                                            .add(ticketDetailsDto.individualTicketType().getDiscount());
                                    Money ticketPrice = computeTicketPrice(
                                            tuple.getT1().baseTicketPrice(), totalDiscount);
                                    return Ticket.builder()
                                            .position(ticketDetailsDto.position())
                                            .type(ticketDetailsDto.individualTicketType())
                                            .ticketStatus(TicketStatus.PURCHASED)
                                            .discount(totalDiscount)
                                            .price(ticketPrice)
                                            .build();
                                })
                                .collect(Collectors.toList()))
                        .build()))
                .flatMap(ticketPurchase ->
                        ticketPort.addOrUpdateMany(ticketPurchase.tickets())
                                .collectList()
                                .flatMap(savedTickets -> ticketPurchasePort.addOrUpdate(ticketPurchase.withTickets(savedTickets))));

        return transactionPort.inTransaction(result).map(TicketPurchaseMapper::toDto);
    }

    private Money computeTicketPrice(Money baseTicketPrice, Discount totalDiscount) {
        return baseTicketPrice.multiply(totalDiscount.inverse().value().toPlainString());
    }

    public Mono<TicketPurchaseDto> purchaseTicketFromOrder(String username, String ticketOrderId) {
        if (isNull(ticketOrderId) || StringUtils.isBlank(ticketOrderId)) {
            return Mono.error(new TicketPurchaseServiceException("Ticket order id is required and cannot be empty"));
        }

        Mono<TicketPurchase> result = ticketOrderPort.findById(ticketOrderId)
                .switchIfEmpty(Mono.error(new TicketPurchaseServiceException(
                        "No ticket order with id: %s".formatted(ticketOrderId))))
                .flatMap(ticketOrder -> validateTicketOrderOwnership(ticketOrder, username))
                .flatMap(ticketOrder -> {
                    var ticketPurchase = ticketOrder.toTicketPurchase();
                    return ticketPort.addOrUpdateMany(ticketPurchase.tickets())
                                .collectList()
                                .flatMap(purchasedTickets -> ticketOrderPort
                                        .addOrUpdate(ticketOrder.changeOrderStatusToDone().withTickets(purchasedTickets))
                                        .then(ticketPurchasePort.addOrUpdate(ticketPurchase.withTickets(purchasedTickets))));
                });

        return transactionPort.inTransaction(result).map(TicketPurchaseMapper::toDto);
    }

    private Mono<TicketOrder> validateTicketOrderOwnership(TicketOrder ticketOrder, String username) {
        if (!Objects.equals(username, ticketOrder.user().username())) {
            return Mono.error(new TicketPurchaseServiceException("Ticket order is not done by you!"));
        }
        if (ticketOrder.ticketOrderStatus() != TicketOrderStatus.ORDERED) {
            return Mono.error(new TicketPurchaseServiceException("Only ordered tickets can be purchased"));
        }
        if (!ticketOrder.movieEmission().startDateTime().toLocalDate()
                .isAfter(LocalDate.now())) {
            return Mono.error(new TicketPurchaseServiceException("You cannot purchase a ticket for an emission that has already started"));
        }
        return Mono.just(ticketOrder);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByUser(String username) {
        return ticketPurchasePort
                .findAllByUserUsername(username)
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByUserAndCity(String username, String cityName) {
        return getAllTicketPurchasesByCityUtility(cityName,
                ids -> ticketPurchasePort.findAllByCinemaHallsIdsAndUsername(ids, username));
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByCity(String cityName) {
        return getAllTicketPurchasesByCityUtility(cityName, ticketPurchasePort::findAllByCinemaHallsIds);
    }

    private Flux<TicketPurchaseDto> getAllTicketPurchasesByCityUtility(String cityName,
                                                                       Function<List<String>, Flux<TicketPurchase>> purchasesFetcher) {
        if (isNull(cityName) || StringUtils.isBlank(cityName)) {
            return Flux.error(() -> new TicketPurchaseServiceException("City name is required and cannot be empty"));
        }

        return cityPort.findByName(cityName)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No city with name %s".formatted(cityName))))
                .map(city -> city.cinemas()
                        .stream()
                        .flatMap(cinema -> cinema.cinemaHalls().stream())
                        .map(CinemaHall::id)
                        .collect(Collectors.toList()))
                .flatMapMany(purchasesFetcher::apply)
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchaseByCinema(String cinemaId) {
        if (isNull(cinemaId) || StringUtils.isBlank(cinemaId)) {
            return Flux.error(() -> new TicketPurchaseServiceException("Cinema id is required and cannot be empty"));
        }

        return cinemaPort.findById(cinemaId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No cinema with id: %s".formatted(cinemaId))))
                .flatMapMany(cinema -> ticketPurchasePort
                        .findAllByCinemaHallsIds(
                                cinema.cinemaHalls().stream()
                                        .map(CinemaHall::id)
                                        .collect(Collectors.toList())))
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByCinemaAndUsername(String cinemaId, String username) {
        if (isNull(cinemaId) || StringUtils.isBlank(cinemaId)) {
            return Flux.error(() -> new TicketPurchaseServiceException("Cinema id is required and cannot be empty"));
        }

        return cinemaPort.findById(cinemaId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No cinema with id: %s".formatted(cinemaId))))
                .flatMapMany(cinema -> ticketPurchasePort
                        .findAllByCinemaHallsIdsAndUsername(
                                cinema.cinemaHalls().stream()
                                        .map(CinemaHall::id)
                                        .collect(Collectors.toList()), username))
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchases() {
        return ticketPurchasePort
                .findAll()
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByDate(String from, String to) {
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        if (StringUtils.isBlank(from) && StringUtils.isBlank(to)) {
            return Flux.error(new TicketPurchaseServiceException("At least one of dates [from, to] is required!"));
        }

        boolean validFrom = StringUtils.isBlank(from) || GenericValidator.isDate(from, "dd-MM-yyyy", true);
        boolean validTo = StringUtils.isBlank(to) || GenericValidator.isDate(to, "dd-MM-yyyy", true);

        if (!validFrom && !validTo) {
            return Flux.error(new TicketPurchaseServiceException("Date from and date to has not valid format"));
        }
        if (!validFrom) {
            return Flux.error(new TicketPurchaseServiceException("Date from has not valid format"));
        }
        if (!validTo) {
            return Flux.error(new TicketPurchaseServiceException("Date to has not valid format"));
        }

        Optional<LocalDate> fromDate = StringUtils.isBlank(from)
                ? Optional.empty()
                : Optional.of(LocalDate.parse(from, fmt));
        Optional<LocalDate> toDate = StringUtils.isBlank(to)
                ? Optional.empty()
                : Optional.of(LocalDate.parse(to, fmt));

        if (fromDate.isPresent() && toDate.isPresent() && fromDate.get().isAfter(toDate.get())) {
            return Flux.error(new TicketPurchaseServiceException("From date cannot be after to date!"));
        }

        if (fromDate.isPresent() && toDate.isPresent()) {
            return ticketPurchasePort.findAllByPurchaseDateBetween(fromDate.get(), toDate.get())
                    .map(TicketPurchaseMapper::toDto);
        }
        return fromDate.map(localDate -> ticketPurchasePort.findAllByPurchaseDateAfter(localDate)
                .map(TicketPurchaseMapper::toDto)).orElseGet(() -> ticketPurchasePort.findAllByPurchaseDateBefore(toDate.get())
                .map(TicketPurchaseMapper::toDto));
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesWithMovieId(String movieId) {
        return moviePort
                .findById(movieId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No movie with id: %s".formatted(movieId))))
                .flatMapMany(movie -> ticketPurchasePort.findAllByMovieId(movie.id()))
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesForUsernameAndMovieId(String username, String movieId) {
        return moviePort
                .findById(movieId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No movie with id: %s".formatted(movieId))))
                .flatMapMany(movie -> ticketPurchasePort
                        .findAllByMovieIdAndUserUsername(movie.id(), username))
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByCinemaHallId(String cinemaHallId) {
        return cinemaHallPort
                .findById(cinemaHallId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No cinema hall with id: %s".formatted(cinemaHallId))))
                .flatMapMany(cinemaHall -> ticketPurchasePort.findAllByCinemaHallId(cinemaHall.id()))
                .map(TicketPurchaseMapper::toDto);
    }
}
