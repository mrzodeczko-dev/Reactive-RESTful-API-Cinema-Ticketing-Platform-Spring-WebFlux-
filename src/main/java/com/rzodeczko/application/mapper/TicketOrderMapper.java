package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.TicketOrderDto;
import com.rzodeczko.domain.ticket_order.TicketOrder;

import java.util.stream.Collectors;

public final class TicketOrderMapper {

    private TicketOrderMapper() {
    }

    public static TicketOrderDto toDto(TicketOrder o) {
        if (o == null) {
            return null;
        }
        return TicketOrderDto.builder()
                .id(o.id())
                .username(o.user() == null ? null : o.user().username())
                .movieEmissionDto(MovieEmissionMapper.toDto(o.movieEmission()))
                .orderDate(o.orderDate())
                .ticketGroupType(o.ticketGroupType())
                .ticketOrderStatus(o.ticketOrderStatus())
                .tickets(o.tickets() == null ? null :
                        o.tickets().stream().map(TicketMapper::toDto).collect(Collectors.toList()))
                .build();
    }
}
