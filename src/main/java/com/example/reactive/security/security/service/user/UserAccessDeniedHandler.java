package com.example.reactive.security.security.service.user;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.buffer.DataBuffer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
@Component
public class UserAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        var response = exchange.getResponse();

        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Map<String, String>> errors =
                Map.of("errors", Map.of("Access-Denied", denied.getMessage()));

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errors);
            DataBuffer dataBuffer = response.bufferFactory().wrap(bytes);

            return response.writeWith(Mono.just(dataBuffer));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
