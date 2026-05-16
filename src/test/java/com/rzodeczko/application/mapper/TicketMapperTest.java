package com.rzodeczko.application.mapper;

import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.domain.ticket.enums.IndividualTicketType;
import com.rzodeczko.domain.ticket.enums.TicketStatus;
import com.rzodeczko.domain.vo.Discount;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketMapperTest {

    @Nested
    @DisplayName("toDto()")
    class ToDtoTests {

        @Test
        @DisplayName("Null ticket: null DTO")
        void shouldReturnNullWhenTicketIsNull() {
            assertThat(TicketMapper.toDto(null)).isNull();
        }

        @Test
        @DisplayName("Ticket: all fields mapped")
        void shouldMapTicketToDto() {
            Position position = new Position(2, 3);
            Discount discount = Discount.of("0.2");
            Money price = Money.of("28.00");
            Ticket ticket = Ticket.builder()
                    .id("ticket-1")
                    .ticketStatus(TicketStatus.PURCHASED)
                    .type(IndividualTicketType.STUDENT)
                    .position(position)
                    .discount(discount)
                    .price(price)
                    .build();

            var dto = TicketMapper.toDto(ticket);

            assertThat(dto.id()).isEqualTo("ticket-1");
            assertThat(dto.ticketStatus()).isEqualTo(TicketStatus.PURCHASED);
            assertThat(dto.type()).isEqualTo(IndividualTicketType.STUDENT);
            assertThat(dto.position()).isSameAs(position);
            assertThat(dto.discount()).isSameAs(discount);
            assertThat(dto.price()).isSameAs(price);
        }
    }
}
