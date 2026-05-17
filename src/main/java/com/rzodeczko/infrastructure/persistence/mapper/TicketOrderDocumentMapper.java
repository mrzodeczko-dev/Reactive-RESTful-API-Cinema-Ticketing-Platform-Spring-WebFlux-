package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.ticket_order.TicketOrder;
import com.rzodeczko.infrastructure.persistence.document.TicketOrderDocument;

public final class TicketOrderDocumentMapper {

    private TicketOrderDocumentMapper() {
    }

    public static TicketOrderDocument toDocument(TicketOrder o) {
        if (o == null) return null;
        return new TicketOrderDocument(
                o.id(),
                UserDocumentMapper.toDocument(o.user()),
                o.orderDate(),
                o.ticketOrderStatus(),
                MovieEmissionDocumentMapper.toDocument(o.movieEmission()),
                TicketDocumentMapper.toDocuments(o.tickets()),
                o.ticketGroupType());
    }

    public static TicketOrder toDomain(TicketOrderDocument doc) {
        if (doc == null) return null;
        return TicketOrder.builder()
                .id(doc.getId())
                .user(UserDocumentMapper.toDomain(doc.getUser()))
                .orderDate(doc.getOrderDate())
                .ticketOrderStatus(doc.getTicketOrderStatus())
                .movieEmission(MovieEmissionDocumentMapper.toDomain(doc.getMovieEmission()))
                .tickets(TicketDocumentMapper.toDomains(doc.getTickets()))
                .ticketGroupType(doc.getTicketGroupType())
                .build();
    }
}
