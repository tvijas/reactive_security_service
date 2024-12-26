package com.example.reactive.security.security.service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OauthFailureHandler implements ServerAuthenticationFailureHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
        Map<String, Map<String, String>> errors = Map.of(
                "errors", Map.of("Authorization", exception.getMessage())
        );
        var response = webFilterExchange.getExchange().getResponse();

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = response.bufferFactory();
        return response.writeWith(
                Mono.fromSupplier(() -> {
                    try {
                        byte[] bytes = objectMapper.writeValueAsBytes(errors);
                        return bufferFactory.wrap(bytes);
                    } catch (IOException ex) {
                        throw new RuntimeException("Error while writing response", ex);
                    }
                })
        );
    }
}
