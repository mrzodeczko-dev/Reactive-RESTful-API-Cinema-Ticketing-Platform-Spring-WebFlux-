package com.rzodeczko.application.dto;

/**
 * Standard error response envelope
 */
public record ResponseErrorDto(
        ErrorMessageDto error,
        String requestId
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ErrorMessageDto error;
        private String requestId;

        public Builder error(ErrorMessageDto error) {
            this.error = error;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public ResponseErrorDto build() {
            return new ResponseErrorDto(error, requestId);
        }
    }
}