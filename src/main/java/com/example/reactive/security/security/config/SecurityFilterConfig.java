package com.example.reactive.security.security.config;

import com.example.reactive.security.security.filter.JwtAuthFilter;
import com.example.reactive.security.security.service.user.OauthFailureHandler;
import com.example.reactive.security.security.service.user.OauthSuccessHandler;
import com.example.reactive.security.security.service.user.UserAccessDeniedHandler;
import com.example.reactive.security.security.service.user.UserAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class SecurityFilterConfig {
    private final OauthFailureHandler oauthFailureHandler;
    private final OauthSuccessHandler oauthSuccessHandler;
    private final UserAuthenticationEntryPoint authenticationEntryPoint;
    private final UserAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(Customizer.withDefaults())
                .authorizeExchange(authorize -> authorize
                        .pathMatchers(
                                HttpMethod.GET,
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()

                        .pathMatchers(HttpMethod.POST, "/api/user/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2Login(oAuth2LoginSpec -> oAuth2LoginSpec
                        .authenticationFailureHandler(oauthFailureHandler)
                        .authenticationSuccessHandler(oauthSuccessHandler))
                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
