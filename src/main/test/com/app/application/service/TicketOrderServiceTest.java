package com.app.application.service;

import com.app.application.dto.CreateTicketOrderDto;
import com.app.application.dto.TicketDetailsDto;
import com.app.application.exception.TicketOrderServiceException;
import com.app.application.validator.CreateTicketsOrderDtoValidator;
import com.app.domain.movie.Movie;
import com.app.domain.movie_emission.MovieEmission;
import com.app.domain.movie_emission.MovieEmissionRepository;
import com.app.domain.security.User;
import com.app.domain.security.UserRepository;
import com.app.domain.ticket.Ticket;
import com.app.domain.ticket.TicketRepository;
import com.app.domain.ticket.enums.IndividualTicketType;
import com.app.domain.ticket.enums.TicketStatus;
import com.app.domain.ticket_order.TicketOrder;
import com.app.domain.ticket_order.TicketOrderRepository;
import com.app.domain.ticket_order.enums.TicketGroupType;
import com.app.domain.ticket_order.enums.TicketOrderStatus;
import com.app.domain.vo.Discount;
import com.app.domain.vo.Money;
import com.app.domain.vo.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketOrderServiceTest {

    @Mock private TicketOrderRepository ticketOrderRepository;
    @Mock private MovieEmissionRepository movieEmissionRepository;
    @Mock private UserRepository userRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private CreateTicketsOrderDtoValidator createTicketsOrderDtoValidator;
    @Mock private TransactionalOperator transactionalOperator;

    @InjectMocks
    private TicketOrderService ticketOrderService;

    private Position positionR1C1;
    private Position positionR1C2;
    private Principal principalJan;
    private User userJan;
    private Movie dummyMovie;
    private MovieEmission emissionWithFreeSeats;
    private MovieEmission fullyBookedEmission;
    private CreateTicketOrderDto validCreateDto;
    private TicketOrder savedOrder;

    @BeforeEach
    void setUp() {
        positionR1C1 = Position.builder().rowNo(1).colNo(1).build();
        positionR1C2 = Position.builder().rowNo(1).colNo(2).build();

        principalJan = () -> "jan@example.com";

        userJan = User.builder()
                .username("jan@example.com")
                .password("hashed-pass")
                .birthDate(LocalDate.of(1995, 5, 20))
                .email("jan@example.com")
                .build();

        dummyMovie = Movie.builder()
                .id("movie-1")
                .name("Test Movie")
                .genre("Drama")
                .duration(120)
                .premiereDate(LocalDate.of(2026, 1, 1))
                .build();

        Map<Position, Boolean> freeSeats = new HashMap<>();
        freeSeats.put(positionR1C1, true);
        freeSeats.put(positionR1C2, true);

        emissionWithFreeSeats = MovieEmission.builder()
                .id("emission-1")
                .movie(dummyMovie)
                .startDateTime(LocalDateTime.of(2026, 6, 15, 18, 30))
                .cinemaHallId("hall-42")
                .isPositionFree(freeSeats)
                .baseTicketPrice(Money.of("25"))
                .build();

        Map<Position, Boolean> takenSeats = new HashMap<>();
        takenSeats.put(positionR1C1, false);
        takenSeats.put(positionR1C2, false);

        fullyBookedEmission = MovieEmission.builder()
                .id("emission-2")
                .movie(dummyMovie)
                .startDateTime(LocalDateTime.of(2026, 6, 16, 20, 0))
                .cinemaHallId("hall-42")
                .isPositionFree(takenSeats)
                .baseTicketPrice(Money.of("25"))
                .build();

        TicketDetailsDto ticketDetail = TicketDetailsDto.builder()
                .position(positionR1C1)
                .individualTicketType(IndividualTicketType.STUDENT)
                .build();

        validCreateDto = CreateTicketOrderDto.builder()
                .movieEmissionId("emission-1")
                .ticketGroupType(TicketGroupType.NORMAL)
                .ticketsDetails(List.of(ticketDetail))
                .build();

        Ticket persistedTicket = Ticket.builder()
                .id("ticket-1")
                .position(positionR1C1)
                .type(IndividualTicketType.STUDENT)
                .ticketStatus(TicketStatus.PURCHASED)
                .discount(Discount.of("0.2"))
                .build();

        savedOrder = TicketOrder.builder()
                .id("order-1")
                .orderDate(LocalDate.of(2026, 5, 3))
                .ticketOrderStatus(TicketOrderStatus.ORDERED)
                .user(userJan)
                .movieEmission(emissionWithFreeSeats)
                .ticketGroupType(TicketGroupType.NORMAL)
                .tickets(List.of(persistedTicket))
                .build();
    }

    @Nested
    @DisplayName("addTicketOrder()")
    class AddTicketOrderTests {

        @Test
        @DisplayName("Happy path: valid DTO returns TicketOrderDto with ORDERED status")
        void shouldCreateOrderSuccessfully() {
            when(createTicketsOrderDtoValidator.validate(validCreateDto)).thenReturn(Map.of());
            when(movieEmissionRepository.findById("emission-1")).thenReturn(Mono.just(emissionWithFreeSeats));
            when(movieEmissionRepository.addOrUpdate(any())).thenReturn(Mono.just(emissionWithFreeSeats));
            when(userRepository.findByUsername("jan@example.com")).thenReturn(Mono.just(userJan));
            when(ticketRepository.addOrUpdateMany(anyList()))
                    .thenAnswer(inv -> Flux.fromIterable(inv.getArgument(0)));
            when(ticketOrderRepository.addOrUpdate(any())).thenReturn(Mono.just(savedOrder));
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(ticketOrderService.addTicketOrder(Mono.just(principalJan), validCreateDto))
                    .assertNext(dto -> {
                        assertThat(dto.getId()).isEqualTo("order-1");
                        assertThat(dto.getUsername()).isEqualTo("jan@example.com");
                        assertThat(dto.getTicketOrderStatus()).isEqualTo(TicketOrderStatus.ORDERED);
                        assertThat(dto.getTickets()).hasSize(1);
                        assertThat(dto.getTicketGroupType()).isEqualTo(TicketGroupType.NORMAL);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Validation error: validator returns errors")
        void shouldErrorWhenValidationFails() {
            when(createTicketsOrderDtoValidator.validate(validCreateDto))
                    .thenReturn(Map.of("movieEmissionId", "must not be blank"));

            StepVerifier.create(ticketOrderService.addTicketOrder(Mono.just(principalJan), validCreateDto))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(TicketOrderServiceException.class);
                        assertThat(ex.getMessage()).contains("Validation errors");
                    })
                    .verify();
        }

        @Test
        @DisplayName("Emission not found: repository returns empty")
        void shouldErrorWhenMovieEmissionNotFound() {
            when(createTicketsOrderDtoValidator.validate(validCreateDto)).thenReturn(Map.of());
            when(movieEmissionRepository.findById("emission-1")).thenReturn(Mono.empty());
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(ticketOrderService.addTicketOrder(Mono.just(principalJan), validCreateDto))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(TicketOrderServiceException.class);
                        assertThat(ex.getMessage()).contains("emission-1");
                    })
                    .verify();
        }

        @Test
        @DisplayName("Seat unavailable: requested position is not free")
        void shouldErrorWhenPositionNotAvailable() {
            CreateTicketOrderDto dtoBookedEmission = CreateTicketOrderDto.builder()
                    .movieEmissionId("emission-2")
                    .ticketGroupType(TicketGroupType.NORMAL)
                    .ticketsDetails(List.of(TicketDetailsDto.builder()
                            .position(positionR1C1)
                            .individualTicketType(IndividualTicketType.REGULAR)
                            .build()))
                    .build();

            when(createTicketsOrderDtoValidator.validate(dtoBookedEmission)).thenReturn(Map.of());
            when(movieEmissionRepository.findById("emission-2")).thenReturn(Mono.just(fullyBookedEmission));
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(ticketOrderService.addTicketOrder(Mono.just(principalJan), dtoBookedEmission))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(TicketOrderServiceException.class);
                        assertThat(ex.getMessage()).isEqualTo("Positions are not valid");
                    })
                    .verify();
        }

        @Test
        @DisplayName("Transaction: pipeline is wrapped exactly once")
        void shouldWrapPipelineInTransaction() {
            when(createTicketsOrderDtoValidator.validate(validCreateDto)).thenReturn(Map.of());
            when(movieEmissionRepository.findById("emission-1")).thenReturn(Mono.just(emissionWithFreeSeats));
            when(movieEmissionRepository.addOrUpdate(any())).thenReturn(Mono.just(emissionWithFreeSeats));
            when(userRepository.findByUsername("jan@example.com")).thenReturn(Mono.just(userJan));
            when(ticketRepository.addOrUpdateMany(anyList()))
                    .thenAnswer(inv -> Flux.fromIterable(inv.getArgument(0)));
            when(ticketOrderRepository.addOrUpdate(any())).thenReturn(Mono.just(savedOrder));
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(ticketOrderService.addTicketOrder(Mono.just(principalJan), validCreateDto))
                    .expectNextCount(1)
                    .verifyComplete();

            verify(transactionalOperator, times(1)).transactional(any(Mono.class));
        }

        @Test
        @DisplayName("ArgumentCaptor: built TicketOrder has ORDERED status and today's orderDate")
        void shouldBuildTicketOrderWithCorrectFields() {
            when(createTicketsOrderDtoValidator.validate(validCreateDto)).thenReturn(Map.of());
            when(movieEmissionRepository.findById("emission-1")).thenReturn(Mono.just(emissionWithFreeSeats));
            when(movieEmissionRepository.addOrUpdate(any())).thenReturn(Mono.just(emissionWithFreeSeats));
            when(userRepository.findByUsername("jan@example.com")).thenReturn(Mono.just(userJan));
            when(ticketRepository.addOrUpdateMany(anyList()))
                    .thenAnswer(inv -> Flux.fromIterable(inv.getArgument(0)));
            when(ticketOrderRepository.addOrUpdate(any(TicketOrder.class))).thenReturn(Mono.just(savedOrder));
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<TicketOrder> captor = ArgumentCaptor.forClass(TicketOrder.class);

            StepVerifier.create(ticketOrderService.addTicketOrder(Mono.just(principalJan), validCreateDto))
                    .expectNextCount(1)
                    .verifyComplete();

            verify(ticketOrderRepository).addOrUpdate(captor.capture());
            TicketOrder built = captor.getValue();
            assertThat(built.getTicketOrderStatus()).isEqualTo(TicketOrderStatus.ORDERED);
            assertThat(built.getOrderDate()).isEqualTo(LocalDate.now());
            assertThat(built.getUser().getUsername()).isEqualTo("jan@example.com");
            assertThat(built.getTickets()).hasSize(1);
        }

        @Test
        @DisplayName("Tickets are saved before order is persisted")
        void shouldSaveTicketsBeforeOrder() {
            when(createTicketsOrderDtoValidator.validate(validCreateDto)).thenReturn(Map.of());
            when(movieEmissionRepository.findById("emission-1")).thenReturn(Mono.just(emissionWithFreeSeats));
            when(movieEmissionRepository.addOrUpdate(any())).thenReturn(Mono.just(emissionWithFreeSeats));
            when(userRepository.findByUsername("jan@example.com")).thenReturn(Mono.just(userJan));
            when(ticketRepository.addOrUpdateMany(anyList()))
                    .thenAnswer(inv -> Flux.fromIterable(inv.getArgument(0)));
            when(ticketOrderRepository.addOrUpdate(any())).thenReturn(Mono.just(savedOrder));
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(ticketOrderService.addTicketOrder(Mono.just(principalJan), validCreateDto))
                    .expectNextCount(1)
                    .verifyComplete();

            verify(ticketRepository).addOrUpdateMany(anyList());
            verify(ticketOrderRepository).addOrUpdate(any());
        }

        @Test
        @DisplayName("Ticket save error: exception is propagated and order is not persisted")
        void shouldPropagateErrorFromTicketRepository() {
            when(createTicketsOrderDtoValidator.validate(validCreateDto)).thenReturn(Map.of());
            when(movieEmissionRepository.findById("emission-1")).thenReturn(Mono.just(emissionWithFreeSeats));
            when(movieEmissionRepository.addOrUpdate(any())).thenReturn(Mono.just(emissionWithFreeSeats));
            when(userRepository.findByUsername("jan@example.com")).thenReturn(Mono.just(userJan));
            when(ticketRepository.addOrUpdateMany(anyList()))
                    .thenReturn(Flux.error(new RuntimeException("MongoDB: write timeout")));
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(ticketOrderService.addTicketOrder(Mono.just(principalJan), validCreateDto))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(RuntimeException.class);
                        assertThat(ex.getMessage()).contains("write timeout");
                    })
                    .verify();

            verifyNoInteractions(ticketOrderRepository);
        }
    }

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrderTests {

        @Test
        @DisplayName("Happy path: owned order is cancelled")
        void shouldCancelOrderSuccessfully() {
            when(ticketOrderRepository.findById("order-1")).thenReturn(Mono.just(savedOrder));

            StepVerifier.create(ticketOrderService.cancelOrder("jan@example.com", "order-1"))
                    .assertNext(dto -> {
                        assertThat(dto.getId()).isEqualTo("order-1");
                        assertThat(dto.getTicketOrderStatus()).isEqualTo(TicketOrderStatus.CANCELLED);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Wrong user: cancelling someone else's order throws exception")
        void shouldThrowWhenOrderBelongsToAnotherUser() {
            when(ticketOrderRepository.findById("order-1")).thenReturn(Mono.just(savedOrder));

            StepVerifier.create(ticketOrderService.cancelOrder("hacker@example.com", "order-1"))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(TicketOrderServiceException.class);
                        assertThat(ex.getMessage()).contains("does not belong to you");
                    })
                    .verify();
        }

        @Test
        @DisplayName("Null order ID: throws synchronous exception")
        void shouldThrowSynchronouslyWhenOrderIdIsNull() {
            assertThatThrownBy(() -> ticketOrderService.cancelOrder("jan@example.com", null))
                    .isInstanceOf(TicketOrderServiceException.class)
                    .hasMessageContaining("null");

            verifyNoInteractions(ticketOrderRepository);
        }

        @Test
        @DisplayName("Order not found: Mono completes empty")
        void shouldCompleteEmptyWhenOrderNotFound() {
            when(ticketOrderRepository.findById("non-existent")).thenReturn(Mono.empty());

            StepVerifier.create(ticketOrderService.cancelOrder("jan@example.com", "non-existent"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Status mutation: DTO has CANCELLED instead of ORDERED")
        void shouldReturnCancelledStatus() {
            when(ticketOrderRepository.findById("order-1")).thenReturn(Mono.just(savedOrder));

            StepVerifier.create(ticketOrderService.cancelOrder("jan@example.com", "order-1"))
                    .assertNext(dto -> {
                        assertThat(dto.getTicketOrderStatus()).isEqualTo(TicketOrderStatus.CANCELLED);
                        assertThat(dto.getTicketOrderStatus()).isNotEqualTo(TicketOrderStatus.ORDERED);
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("getAllTicketOrdersForLoggedUser()")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Two orders: repository returns two DTOs in order")
        void shouldReturnAllOrdersMappedToDto() {
            Ticket ticket2 = Ticket.builder()
                    .id("ticket-2")
                    .position(positionR1C2)
                    .type(IndividualTicketType.REGULAR)
                    .ticketStatus(TicketStatus.PURCHASED)
                    .discount(Discount.of("0.0"))
                    .build();

            TicketOrder secondOrder = TicketOrder.builder()
                    .id("order-2")
                    .orderDate(LocalDate.of(2026, 4, 10))
                    .ticketOrderStatus(TicketOrderStatus.DONE)
                    .user(userJan)
                    .movieEmission(emissionWithFreeSeats)
                    .ticketGroupType(TicketGroupType.FAMILY)
                    .tickets(List.of(ticket2))
                    .build();

            when(ticketOrderRepository.findAllByUsername("jan@example.com"))
                    .thenReturn(Flux.just(savedOrder, secondOrder));

            StepVerifier.create(ticketOrderService.getAllTicketOrdersForLoggedUser("jan@example.com"))
                    .assertNext(dto -> {
                        assertThat(dto.getId()).isEqualTo("order-1");
                        assertThat(dto.getTicketOrderStatus()).isEqualTo(TicketOrderStatus.ORDERED);
                        assertThat(dto.getUsername()).isEqualTo("jan@example.com");
                    })
                    .assertNext(dto -> {
                        assertThat(dto.getId()).isEqualTo("order-2");
                        assertThat(dto.getTicketOrderStatus()).isEqualTo(TicketOrderStatus.DONE);
                        assertThat(dto.getTicketGroupType()).isEqualTo(TicketGroupType.FAMILY);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("No orders: repository returns empty Flux")
        void shouldReturnEmptyFluxWhenNoOrders() {
            when(ticketOrderRepository.findAllByUsername("jan@example.com"))
                    .thenReturn(Flux.empty());

            StepVerifier.create(ticketOrderService.getAllTicketOrdersForLoggedUser("jan@example.com"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Repository error: runtime exception is propagated")
        void shouldPropagateRepositoryError() {
            when(ticketOrderRepository.findAllByUsername("jan@example.com"))
                    .thenReturn(Flux.error(new RuntimeException("MongoDB: connection refused")));

            StepVerifier.create(ticketOrderService.getAllTicketOrdersForLoggedUser("jan@example.com"))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(RuntimeException.class);
                        assertThat(ex.getMessage()).contains("connection refused");
                    })
                    .verify();
        }

        @Test
        @DisplayName("No status filter: ORDERED and CANCELLED orders are both returned")
        void shouldNotFilterByStatus() {
            TicketOrder cancelledOrder = TicketOrder.builder()
                    .id("order-3")
                    .orderDate(LocalDate.of(2026, 3, 1))
                    .ticketOrderStatus(TicketOrderStatus.CANCELLED)
                    .user(userJan)
                    .movieEmission(emissionWithFreeSeats)
                    .ticketGroupType(TicketGroupType.NORMAL)
                    .tickets(List.of())
                    .build();

            when(ticketOrderRepository.findAllByUsername("jan@example.com"))
                    .thenReturn(Flux.just(savedOrder, cancelledOrder));

            StepVerifier.create(ticketOrderService.getAllTicketOrdersForLoggedUser("jan@example.com"))
                    .expectNextCount(2)
                    .verifyComplete();
        }
    }
}