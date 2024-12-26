package com.example.reactive.security.security.service.user;

import com.example.reactive.security.security.models.CustomOAuth2User;
import com.example.reactive.security.security.service.jwt.JwtGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OauthSuccessHandler implements ServerAuthenticationSuccessHandler {
    private final JwtGeneratorService jwtGeneratorService;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        var response = webFilterExchange.getExchange().getResponse();

        return jwtGeneratorService.generateTokens(oauthUser.getUser()).flatMap(tokenPair -> {
            response.getHeaders().add("Authorization", "Bearer " + tokenPair.getAccessTokenValue());
            response.getHeaders().add("X-Refresh-Token", tokenPair.getRefreshTokenValue());
            response.setStatusCode(HttpStatus.OK);
            return response.writeWith(Mono.empty());
        });
    }
}
