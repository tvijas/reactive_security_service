package com.example.reactive.security.security.service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    @Value("${frontend.url}")
    private String frontEndUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        Map<String, Map<String, String>> errors = Map.of(
                "errors", Map.of("Authorization", ex.getMessage())
        );
        var response = exchange.getResponse();

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = response.bufferFactory();
        return response.writeWith(
                Mono.fromSupplier(() -> {
                    try {
                        byte[] bytes = objectMapper.writeValueAsBytes(errors);
                        return bufferFactory.wrap(bytes);
                    } catch (IOException e) {
                        throw new RuntimeException("Error while writing response", e);
                    }
                })
        );
    }
}
