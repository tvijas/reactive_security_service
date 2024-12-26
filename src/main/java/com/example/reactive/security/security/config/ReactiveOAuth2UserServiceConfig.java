package com.example.reactive.security.security.config;

import com.example.reactive.security.security.service.user.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Configuration
@RequiredArgsConstructor
public class ReactiveOAuth2UserServiceConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    @Bean
    public ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return customOAuth2UserService;
    }
}
