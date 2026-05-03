package com.app.application.service;

import com.app.application.exception.TicketOrderServiceException;
import com.app.application.validator.CreateTicketsOrderDtoValidator;
import com.app.domain.movie.Movie;
import com.app.domain.movie_emission.MovieEmission;
import com.app.domain.movie_emission.MovieEmissionRepository;
import com.app.domain.security.User;
import com.app.domain.security.UserRepository;
import com.app.domain.ticket.TicketRepository;
import com.app.domain.ticket_order.TicketOrder;
import com.app.domain.ticket_order.TicketOrderRepository;
import com.app.domain.ticket_order.enums.TicketOrderStatus;
import com.app.domain.vo.Money;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketOrderServiceTest {

    @Mock
    private TicketOrderRepository ticketOrderRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private UserRepository userRepository;                          // bez 's'
    @Mock
    private MovieEmissionRepository movieEmissionRepository;        // NOWY
    @Mock
    private CreateTicketsOrderDtoValidator createTicketsOrderDtoValidator; // NOWY
    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private TicketOrderService ticketOrderService;

    private TicketOrder sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = TicketOrder.builder()
                .id("order-1")
                .user(User.builder()
                        .username("test@example.com")
                        .build())
                .movieEmission(MovieEmission.builder()
                        .baseTicketPrice(Money.of("50"))
                        .movie(Movie.builder()
                                .name("Sample Movie")
                                .build())
                        .id("emission-1")
                        .build())
                .tickets(List.of())
                .orderDate(LocalDate.now())
                .ticketOrderStatus(TicketOrderStatus.ORDERED)
                .build();
    }

    @Test
    @DisplayName("getAllTicketOrdersForLoggedUser — returns orders for user")
    void getAllTicketOrders_shouldReturnOrders() {
        when(ticketOrderRepository.findAllByUsername("test@example.com"))
                .thenReturn(Flux.just(sampleOrder));

        StepVerifier.create(ticketOrderService.getAllTicketOrdersForLoggedUser("test@example.com"))
                .assertNext(dto -> assertThat(dto.getId()).isEqualTo("order-1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getAllTicketOrdersForLoggedUser — empty when no orders")
    void getAllTicketOrders_whenNoOrders_shouldReturnEmpty() {
        when(ticketOrderRepository.findAllByUsername("test@example.com"))
                .thenReturn(Flux.empty());

        StepVerifier.create(ticketOrderService.getAllTicketOrdersForLoggedUser("test@example.com"))
                .verifyComplete();
    }

    @Test
    @DisplayName("cancelOrder — throws synchronously when orderId is null")
    void cancelOrder_whenOrderIdNull_shouldThrow() {
        assertThatThrownBy(() -> ticketOrderService.cancelOrder("user", null))
                .isInstanceOf(TicketOrderServiceException.class)
                .hasMessageContaining("null");
    }

    @Test
    @DisplayName("cancelOrder — throws when order belongs to different user")
    void cancelOrder_whenOrderNotOwned_shouldError() {
        var order = TicketOrder.builder()
                .id("order-1")
                .user(User.builder().username("other@example.com").build())
                .build();

        when(ticketOrderRepository.findById("order-1")).thenReturn(Mono.just(order));

        StepVerifier.create(ticketOrderService.cancelOrder("attacker@example.com", "order-1"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(TicketOrderServiceException.class)
                        .hasMessageContaining("does not belong"))
                .verify();
    }
}