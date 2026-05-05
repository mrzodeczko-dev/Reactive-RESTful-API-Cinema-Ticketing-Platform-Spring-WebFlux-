package com.app.application.validator;

import com.app.application.dto.CreateTicketOrderDto;
import com.app.application.validator.generic.Validator;
import com.app.application.validator.util.TicketBaseValidationUtils;
import com.app.domain.ticket_order.enums.TicketGroupType;

import java.util.Map;

import static java.util.Objects.nonNull;

public class CreateTicketsOrderDtoValidator implements Validator<CreateTicketOrderDto, String> {

    @Override
    public Map<String, String> validate(CreateTicketOrderDto item) {
        return validateTicketOrder(TicketBaseValidationUtils.validate(item), item);
    }

    private boolean isTicketOrderTypeValid(TicketGroupType ticketGroupType) {
        return nonNull(ticketGroupType);
    }

    private Map<String, String> validateTicketOrder(Map<String, String> currentErrors, CreateTicketOrderDto item) {

        if (nonNull(item) && !isTicketOrderTypeValid(item.getTicketGroupType())) {
            currentErrors.put("ticketOrderType {%s}".formatted(item.getTicketGroupType()), "is not valid");
        }

        return currentErrors;
    }

}
