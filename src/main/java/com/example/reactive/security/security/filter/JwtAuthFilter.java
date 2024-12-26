package com.example.reactive.security.security.filter;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.reactive.security.foruser.CustomUserDetails;
import com.example.reactive.security.security.models.enums.TokenType;
import com.example.reactive.security.security.service.jwt.JwtValidatorService;
import com.example.reactive.security.security.util.PermittedUrls;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

import static com.example.reactive.security.security.constant.JwtClaimKey.JWT_ID;
import static com.example.reactive.security.security.util.parsers.AuthHeaderParser.recoverToken;
import static com.example.reactive.security.security.util.parsers.jwt.JwtPayloadParser.*;
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {
    //    private final GlobalRateLimit globalRateLimit;
//    @Value("${global.rate.limit.turn.on}")
//    private boolean turnOnRateLimit;
//    private final JwtPayloadValidatorService jwtPayloadValidatorService;
    private final JwtValidatorService jwtValidatorService;
    private final PermittedUrls permittedUrls;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return permittedUrls.isPermitAllRequest(exchange)
                .flatMap(isPermitted -> {
                    log.info("Authenticating request passing permission");
                    if (isPermitted) {
                        return chain.filter(exchange);
                    }

                    log.info("Authenticating request passed permission");
                    log.info("Trying to recover token");
                    Optional<String> token = recoverToken(exchange.getRequest());
                    if (token.isEmpty()) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    log.info("Token is recovered");

                    Optional<DecodedJWT> optionalDecodedAccessToken = jwtValidatorService
                            .validateToken(token.get(), TokenType.ACCESS);

                    if (optionalDecodedAccessToken.isEmpty()) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    DecodedJWT decodedAccessToken = optionalDecodedAccessToken.get();
                    Map<String, Claim> claims = parsePayloadFromDecodedJwt(decodedAccessToken);
                    CustomUserDetails userDetails = getUserDetailsFromClaims(claims);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails.getPrincipal(), null, userDetails.getAuthorities());

                    log.info("Filtering user request");

                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                });
        }

    private String getClientIpAddress(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return ip;
    }
}
