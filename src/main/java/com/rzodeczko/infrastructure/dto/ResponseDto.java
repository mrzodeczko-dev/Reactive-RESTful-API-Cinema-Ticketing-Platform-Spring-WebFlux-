package com.rzodeczko.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rzodeczko.application.dto.ErrorMessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto<T> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ErrorMessageDto error;
}
