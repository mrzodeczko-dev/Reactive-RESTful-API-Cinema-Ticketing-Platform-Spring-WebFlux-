package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.TicketPurchaseDto;
import com.rzodeczko.domain.ticket_purchase.TicketPurchase;

import java.util.stream.Collectors;

public final class TicketPurchaseMapper {

    private TicketPurchaseMapper() {
    }

    public static TicketPurchaseDto toDto(TicketPurchase ticketPurchase) {
        if (ticketPurchase == null) {
            return null;
        }
        return TicketPurchaseDto.builder()
                .id(ticketPurchase.id())
                .username(ticketPurchase.user() == null ? null : ticketPurchase.user().username())
                .movieEmissionDto(MovieEmissionMapper.toDto(ticketPurchase.movieEmission()))
                .purchaseDate(ticketPurchase.purchaseDate())
                .tickets(ticketPurchase.tickets() == null ? null :
                        ticketPurchase.tickets().stream().map(TicketMapper::toDto).collect(Collectors.toList()))
                .ticketGroupType(ticketPurchase.ticketGroupType())
                .build();
    }
}
