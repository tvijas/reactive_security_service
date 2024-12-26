package com.example.reactive.security.security.config;

import com.example.reactive.security.security.service.user.UserAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class ReactiveAuthenticationManagerConfig {
    @Bean
    public ReactiveAuthenticationManager authenticationManager(UserAuthenticationProvider userAuthenticationProvider) {
        return userAuthenticationProvider;
    }
}
