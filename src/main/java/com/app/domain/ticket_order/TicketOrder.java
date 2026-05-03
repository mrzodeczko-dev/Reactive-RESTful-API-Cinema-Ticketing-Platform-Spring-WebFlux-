package com.app.domain.ticket_order;

import com.app.application.dto.TicketOrderDto;
import com.app.domain.generic.GenericEntity;
import com.app.domain.movie_emission.MovieEmission;
import com.app.domain.security.User;
import com.app.domain.ticket.Ticket;
import com.app.domain.ticket_order.enums.TicketGroupType;
import com.app.domain.ticket_order.enums.TicketOrderStatus;
import com.app.domain.ticket_purchase.TicketPurchase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document("ticket_orders")
public class TicketOrder implements GenericEntity {

    @Id
    @Getter
    private String id;

    @Getter
    private User user;

    @Getter
    private LocalDate orderDate;

    @Getter
    private TicketOrderStatus ticketOrderStatus;

    @Getter
    private MovieEmission movieEmission;

    @Getter
    private List<Ticket> tickets;

    @Field("ticket_order_type")
    private TicketGroupType ticketGroupType;

    public TicketPurchase toTicketPurchase() {
        return TicketPurchase.builder()
                .purchaseDate(LocalDate.now())
                .ticketGroupType(ticketGroupType)
                .movieEmission(movieEmission)
                .tickets(tickets)
                .user(user)
                .build();
    }

    public TicketOrder changeOrderStatusToDone() {
        ticketOrderStatus = TicketOrderStatus.DONE;
        return this;
    }

    public TicketOrder changeOrderStatusToCancelled() {
        ticketOrderStatus = TicketOrderStatus.CANCELLED;
        return this;
    }

    public TicketOrderDto toDto() {
        return TicketOrderDto.builder()
                .id(id)
                .username(user.getUsername())
                .movieEmissionDto(movieEmission.toDto())
                .orderDate(orderDate)
                .ticketGroupType(ticketGroupType)
                .ticketOrderStatus(ticketOrderStatus)
                .tickets(tickets.stream().map(Ticket::toDto).collect(Collectors.toList()))
                .build();


    }
}



