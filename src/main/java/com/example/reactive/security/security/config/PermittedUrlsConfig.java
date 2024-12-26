package com.example.reactive.security.security.config;

import com.example.reactive.security.security.util.PermittedUrls;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class PermittedUrlsConfig {
    @Bean
    PermittedUrls permittedUrls() {
        return PermittedUrls.builder()
                .addPermitAllMatcher("/api/user/**")
                .addPermitAllMatcher(HttpMethod.POST, "/api/user/token/refresh")
                .addPermitAllMatcher(HttpMethod.GET, "/login/oauth2/code/google/**")
                .addPermitAllMatcher(HttpMethod.GET, "/oauth2/authorization/google")
                .build();
    }
}
