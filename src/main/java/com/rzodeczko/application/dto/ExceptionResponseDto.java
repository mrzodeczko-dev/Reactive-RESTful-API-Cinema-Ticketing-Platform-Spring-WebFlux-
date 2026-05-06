package com.rzodeczko.application.dto;

import lombok.Getter;

@Getter
public class ExceptionResponseDto extends RuntimeException {

    public ExceptionResponseDto(String msg) {
        super(msg);
    }
}
