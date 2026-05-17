package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.TicketDto;
import com.rzodeczko.domain.ticket.Ticket;

public final class TicketMapper {

    private TicketMapper() {
    }

    public static TicketDto toDto(Ticket t) {
        if (t == null) {
            return null;
        }
        return TicketDto.builder()
                .id(t.id())
                .position(t.position())
                .discount(t.discount())
                .type(t.type())
                .ticketStatus(t.ticketStatus())
                .price(t.price())
                .build();
    }
}
