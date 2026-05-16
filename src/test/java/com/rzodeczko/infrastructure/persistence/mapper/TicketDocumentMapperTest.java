package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.domain.ticket.enums.IndividualTicketType;
import com.rzodeczko.domain.ticket.enums.TicketStatus;
import com.rzodeczko.domain.vo.Discount;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;
import com.rzodeczko.infrastructure.persistence.document.TicketDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TicketDocumentMapperTest {

    @Nested
    @DisplayName("toDocument()")
    class ToDocumentTests {

        @Test
        @DisplayName("Null domain: null document")
        void shouldReturnNullWhenDomainIsNull() {
            assertThat(TicketDocumentMapper.toDocument(null)).isNull();
        }

        @Test
        @DisplayName("Ticket domain: all fields mapped")
        void shouldMapDomainToDocument() {
            TicketDocument document = TicketDocumentMapper.toDocument(ticket());

            assertThat(document.getId()).isEqualTo("ticket-1");
            assertThat(document.getTicketStatus()).isEqualTo(TicketStatus.PURCHASED);
            assertThat(document.getType()).isEqualTo(IndividualTicketType.STUDENT);
            assertThat(document.getPosition()).isEqualTo(new Position(2, 3));
            assertThat(document.getDiscount()).isEqualTo(Discount.of("0.2"));
            assertThat(document.getPrice()).isEqualTo(Money.of("28.00"));
        }

        @Test
        @DisplayName("Null list: null documents list")
        void shouldReturnNullDocumentsWhenListIsNull() {
            assertThat(TicketDocumentMapper.toDocuments(null)).isNull();
        }

        @Test
        @DisplayName("Ticket list: documents preserve order")
        void shouldMapDomainListToDocuments() {
            List<TicketDocument> documents = TicketDocumentMapper.toDocuments(List.of(
                    ticket().setId("ticket-1"),
                    ticket().setId("ticket-2")
            ));

            assertThat(documents).extracting(TicketDocument::getId)
                    .containsExactly("ticket-1", "ticket-2");
        }
    }

    @Nested
    @DisplayName("toDomain()")
    class ToDomainTests {

        @Test
        @DisplayName("Null document: null domain")
        void shouldReturnNullWhenDocumentIsNull() {
            assertThat(TicketDocumentMapper.toDomain(null)).isNull();
        }

        @Test
        @DisplayName("Ticket document: all fields mapped")
        void shouldMapDocumentToDomain() {
            Ticket ticket = TicketDocumentMapper.toDomain(document("ticket-1"));

            assertThat(ticket.getId()).isEqualTo("ticket-1");
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.PURCHASED);
            assertThat(ticket.getType()).isEqualTo(IndividualTicketType.STUDENT);
            assertThat(ticket.getPosition()).isEqualTo(new Position(2, 3));
            assertThat(ticket.getDiscount()).isEqualTo(Discount.of("0.2"));
            assertThat(ticket.getPrice()).isEqualTo(Money.of("28.00"));
        }

        @Test
        @DisplayName("Null list: null domains list")
        void shouldReturnNullDomainsWhenListIsNull() {
            assertThat(TicketDocumentMapper.toDomains(null)).isNull();
        }

        @Test
        @DisplayName("Document list: domains preserve order")
        void shouldMapDocumentListToDomains() {
            List<Ticket> tickets = TicketDocumentMapper.toDomains(List.of(
                    document("ticket-1"),
                    document("ticket-2")
            ));

            assertThat(tickets).extracting(Ticket::getId)
                    .containsExactly("ticket-1", "ticket-2");
        }
    }

    private Ticket ticket() {
        return Ticket.builder()
                .id("ticket-1")
                .ticketStatus(TicketStatus.PURCHASED)
                .type(IndividualTicketType.STUDENT)
                .position(new Position(2, 3))
                .discount(Discount.of("0.2"))
                .price(Money.of("28.00"))
                .build();
    }

    private TicketDocument document(String id) {
        return new TicketDocument(
                id,
                TicketStatus.PURCHASED,
                IndividualTicketType.STUDENT,
                new Position(2, 3),
                Discount.of("0.2"),
                Money.of("28.00")
        );
    }
}
