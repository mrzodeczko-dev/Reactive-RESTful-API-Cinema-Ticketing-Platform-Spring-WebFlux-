package com.rzodeczko.presentation.exception;

import com.rzodeczko.application.dto.ErrorMessageDto;
import com.rzodeczko.application.dto.ResponseErrorDto;
import com.rzodeczko.application.exception.AuthenticationException;
import com.rzodeczko.application.exception.HandledException;
import com.rzodeczko.application.exception.RegistrationUserException;
import com.rzodeczko.infrastructure.web.RequestIdWebFilter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Configuration
@Order(-2)
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;
    private final DataBufferFactory dataBufferFactory;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, @NonNull Throwable throwable) {
        HttpStatus status = resolveStatus(throwable);
        String requestId = RequestIdWebFilter.get(exchange);

        log.error("Request failed [reqId={} {} {} -> {}]: {}",
                requestId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath(),
                status.value(),
                throwable.getMessage(),
                throwable);

        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().setStatusCode(status);

        return exchange.getResponse().writeWith(Mono.just(buildBody(throwable, status, requestId)));
    }

    private HttpStatus resolveStatus(Throwable throwable) {
        if (throwable instanceof ResponseStatusException rse) {
            HttpStatus resolved = HttpStatus.resolve(rse.getStatusCode().value());
            return resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (throwable instanceof AuthenticationException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (throwable instanceof RegistrationUserException) {
            String msg = throwable.getMessage();
            if (msg != null && msg.contains("already exists")) {
                return HttpStatus.CONFLICT;
            }
            return HttpStatus.BAD_REQUEST;
        }
        if (throwable instanceof HandledException) {
            String msg = throwable.getMessage();
            if (msg != null && (msg.contains("No ") || msg.contains("not found"))) {
                return HttpStatus.NOT_FOUND;
            }
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private DataBuffer buildBody(Throwable throwable, HttpStatus status, String requestId) {
        // For 5xx don't leak the underlying message — it may carry stack/connection info.
        // For 4xx pass the message through, it's usually a meaningful validation error.
        String clientMessage = status.is5xxServerError()
                ? "Internal server error. Please try again later."
                : throwable.getMessage();

        try {
            return dataBufferFactory.wrap(objectMapper.writeValueAsBytes(
                    ResponseErrorDto.builder()
                            .error(ErrorMessageDto.builder().message(clientMessage).build())
                            .requestId(requestId)
                            .build()));
        } catch (JacksonException ex) {
            log.error("Failed to serialize error response [reqId={}]", requestId, ex);
            return dataBufferFactory.wrap(new byte[]{});
        }
    }
}