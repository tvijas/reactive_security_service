package com.example.reactive.security.security.config;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtAlgorithmConfig {
    @Bean
    Algorithm jwtAlgorithm(@Value("${security.jwt.token.secret-key:secret-key}") String JWT_SECRET) {
        return Algorithm.HMAC256(JWT_SECRET);
    }
}