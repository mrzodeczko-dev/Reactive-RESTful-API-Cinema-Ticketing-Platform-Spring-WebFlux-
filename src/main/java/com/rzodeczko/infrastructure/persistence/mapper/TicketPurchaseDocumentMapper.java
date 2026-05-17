package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.ticket_purchase.TicketPurchase;
import com.rzodeczko.infrastructure.persistence.document.TicketPurchaseDocument;

public final class TicketPurchaseDocumentMapper {

    private TicketPurchaseDocumentMapper() {
    }

    public static TicketPurchaseDocument toDocument(TicketPurchase p) {
        if (p == null) return null;
        return new TicketPurchaseDocument(
                p.id(),
                UserDocumentMapper.toDocument(p.user()),
                p.purchaseDate(),
                MovieEmissionDocumentMapper.toDocument(p.movieEmission()),
                TicketDocumentMapper.toDocuments(p.tickets()),
                p.ticketGroupType());
    }

    public static TicketPurchase toDomain(TicketPurchaseDocument doc) {
        if (doc == null) return null;
        return TicketPurchase.builder()
                .id(doc.getId())
                .user(UserDocumentMapper.toDomain(doc.getUser()))
                .purchaseDate(doc.getPurchaseDate())
                .movieEmission(MovieEmissionDocumentMapper.toDomain(doc.getMovieEmission()))
                .tickets(TicketDocumentMapper.toDomains(doc.getTickets()))
                .ticketGroupType(doc.getTicketGroupType())
                .build();
    }
}
