package com.example.reactive.security.security.util.parsers;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;
import java.util.Optional;

public final class AuthHeaderParser {
    private AuthHeaderParser() {
    }

    public static Optional<String> recoverToken(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty())
            return Optional.empty();
        return recoverToken(authHeaders.get(0));
    }

    public static Optional<String> recoverToken(String headerValue) {
        if (headerValue == null || !headerValue.startsWith("Bearer "))
            return Optional.empty();
        String token = headerValue.substring(7).trim();

        if (token.isEmpty())
            return Optional.empty();

        return Optional.of(token);
    }
}
