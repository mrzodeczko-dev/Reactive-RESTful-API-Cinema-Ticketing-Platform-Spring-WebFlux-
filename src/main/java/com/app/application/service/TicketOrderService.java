package com.app.application.service;

import com.app.application.dto.CreateTicketOrderDto;
import com.app.application.dto.TicketOrderDto;
import com.app.application.exception.TicketOrderServiceException;
import com.app.application.validator.CreateTicketsOrderDtoValidator;
import com.app.application.validator.util.Validations;
import com.app.domain.movie_emission.MovieEmissionRepository;
import com.app.domain.security.UserRepository;
import com.app.domain.ticket.Ticket;
import com.app.domain.ticket.TicketRepository;
import com.app.domain.ticket.enums.TicketStatus;
import com.app.domain.ticket_order.TicketOrder;
import com.app.domain.ticket_order.TicketOrderRepository;
import com.app.domain.ticket_order.enums.TicketOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Objects;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class TicketOrderService {

    private final TicketOrderRepository ticketOrderRepository;
    private final MovieEmissionRepository movieEmissionRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final CreateTicketsOrderDtoValidator createTicketsOrderDtoValidator;
    private final TransactionalOperator transactionalOperator;

    public Mono<TicketOrderDto> addTicketOrder(Mono<? extends Principal> principal, CreateTicketOrderDto createTicketOrderDto) {

        var errors = createTicketsOrderDtoValidator.validate(createTicketOrderDto);
        if (Validations.hasErrors(errors)) {
            return Mono.error(() -> new TicketOrderServiceException(
                    "Validation errors: %s".formatted(Validations.createErrorMessage(errors))));
        }

        return principal
                .flatMap(p -> movieEmissionRepository
                        .findById(createTicketOrderDto.getMovieEmissionId())
                        .switchIfEmpty(Mono.error(() -> new TicketOrderServiceException(
                                "No movie emission with id: %s".formatted(createTicketOrderDto.getMovieEmissionId()))))
                        // Replaced throw inside flatMap with Mono.error
                        .flatMap(movieEmission ->
                                createTicketOrderDto.areAllPositionsAvailable(movieEmission.getFreePositions())
                                        ? Mono.just(movieEmission)
                                        : Mono.error(new TicketOrderServiceException("Positions are not valid")))
                        .flatMap(movieEmission -> movieEmissionRepository
                                .addOrUpdate(movieEmission.removeFreePositions(createTicketOrderDto.getTicketsDetails()))
                                .flatMap(ignored -> userRepository.findByUsername(p.getName()))
                                .map(user -> TicketOrder.builder()
                                        .orderDate(LocalDate.now())
                                        .movieEmission(movieEmission)
                                        .ticketGroupType(createTicketOrderDto.getTicketGroupType())
                                        .ticketOrderStatus(TicketOrderStatus.ORDERED)
                                        .user(user)
                                        .tickets(createTicketOrderDto.getTicketsDetails().stream()
                                                .map(td -> Ticket.builder()
                                                        .position(td.getPosition())
                                                        .type(td.getIndividualTicketType())
                                                        .ticketStatus(TicketStatus.PURCHASED)
                                                        .discount(createTicketOrderDto.getBaseDiscount()
                                                                .add(td.getIndividualTicketType().getDiscount()))
                                                        .build())
                                                .toList())
                                        .build())))
                .flatMap(ticketOrder -> ticketRepository.addOrUpdateMany(ticketOrder.getTickets())
                        .collectList()
                        .flatMap(savedTickets -> ticketOrderRepository.addOrUpdate(
                                ticketOrder.toBuilder().tickets(savedTickets).build()))
                        .map(TicketOrder::toDto))
                .as(transactionalOperator::transactional);
    }

    public Mono<TicketOrderDto> cancelOrder(String username, String orderId) {
        // Synchronous null-check returned as Mono.error instead of throwing directly
        if (isNull(orderId)) {
            return Mono.error(new TicketOrderServiceException("Order id is null"));
        }

        return ticketOrderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new TicketOrderServiceException("No order with id: %s".formatted(orderId))))
                // Replaced throw inside .map() with flatMap + Mono.error
                .flatMap(ticketOrder -> {
                    if (!Objects.equals(ticketOrder.getUser().getUsername(), username)) {
                        return Mono.error(new TicketOrderServiceException("That ticket order does not belong to you"));
                    }
                    return Mono.just(ticketOrder.changeOrderStatusToCancelled());
                })
                .map(TicketOrder::toDto);
    }

    public Flux<TicketOrderDto> getAllTicketOrdersForLoggedUser(String username) {
        return ticketOrderRepository.findAllByUsername(username)
                .map(TicketOrder::toDto);
    }
}
