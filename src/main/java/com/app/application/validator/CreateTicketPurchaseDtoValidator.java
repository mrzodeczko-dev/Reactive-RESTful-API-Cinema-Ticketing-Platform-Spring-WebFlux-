package com.app.application.validator;

import com.app.application.dto.CreateTicketPurchaseDto;
import com.app.application.validator.generic.Validator;
import com.app.application.validator.util.TicketBaseValidationUtils;

import java.util.Map;

public class CreateTicketPurchaseDtoValidator implements Validator<CreateTicketPurchaseDto, String> {

    @Override
    public Map<String, String> validate(CreateTicketPurchaseDto item) {

        return TicketBaseValidationUtils.validate(item);
    }

}
