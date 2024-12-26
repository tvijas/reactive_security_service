package com.example.reactive.security.security.service.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.reactive.security.exceptions.BasicException;
import com.example.reactive.security.foruser.UserEntity;
import com.example.reactive.security.foruser.UserService;
import com.example.reactive.security.security.models.entities.tokens.TokensEntity;
import com.example.reactive.security.security.models.enums.Provider;
import com.example.reactive.security.security.models.enums.TokenType;
import com.example.reactive.security.security.models.tokens.AccessToken;
import com.example.reactive.security.security.models.tokens.RefreshToken;
import com.example.reactive.security.security.models.tokens.TokenPair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.reactive.security.security.constant.JwtClaimKey.*;
import static com.example.reactive.security.security.util.parsers.jwt.JwtPayloadParser.getProviderFromClaims;
import static com.example.reactive.security.security.util.parsers.jwt.JwtPayloadParser.parsePayloadFromDecodedJwt;


@Slf4j
@Service
@RequiredArgsConstructor
public class JwtGeneratorService {
    private long accessTokenDurationInSeconds;
    private long refreshTokenDurationInSeconds;

    private final JwtValidatorService jwtValidatorService;
    private final JwtService jwtService;
    private final UserService userService;
    private final Algorithm algorithm;

    @Autowired
    public JwtGeneratorService(@Value("${security.jwt.access.token.duration.minutes:15}") long accessDuration,
                               @Value("${security.jwt.access.token.duration.days:7}") int refreshDuration,
                               JwtValidatorService jwtValidatorService, JwtService jwtService, UserService userService, Algorithm algorithm) {
        this.accessTokenDurationInSeconds = Duration.ofMinutes(accessDuration).toSeconds();
        this.refreshTokenDurationInSeconds = Duration.ofDays(refreshDuration).toSeconds();
        this.jwtValidatorService = jwtValidatorService;
        this.jwtService = jwtService;
        this.userService = userService;
        this.algorithm = algorithm;
    }

    @Transactional
    public Mono<TokenPair> refreshTokens(String access_token, String refresh_token) {
        DecodedJWT decodedRefreshToken = jwtValidatorService
                .validateToken(refresh_token, TokenType.REFRESH)
                .orElseThrow(() ->
                        new BasicException(Map.of("refreshToken", "Refresh token isn't valid"), HttpStatus.UNAUTHORIZED));

        DecodedJWT decodedAccessToken = jwtValidatorService
                .validateTokenWithoutExp(access_token, TokenType.ACCESS)
                .orElseThrow(() ->
                        new BasicException(Map.of("accessToken", "Access token isn't valid"), HttpStatus.UNAUTHORIZED));

        if (!areTokensLinked(decodedAccessToken, decodedRefreshToken))
            throw new BasicException(Map.of("tokens", "Tokens are not linked too each other"), HttpStatus.BAD_REQUEST);

        Map<String, Claim> claims = parsePayloadFromDecodedJwt(decodedRefreshToken);
        Provider provider = getProviderFromClaims(claims);
        String email = decodedRefreshToken.getSubject();

        Mono<UserEntity> userMono = userService.findByEmailAndProvider(email, provider);

        Instant accessTokenExpiration = calculateExpirationInstantWithMicros(accessTokenDurationInSeconds);
        Instant refreshTokenExpiration = calculateExpirationInstantWithMicros(refreshTokenDurationInSeconds);
        Instant expiresAt = decodedAccessToken.getExpiresAtAsInstant();

        return userMono.switchIfEmpty(Mono.error(new BasicException(
                        Map.of("refreshToken", "Email from token's subject not found"),
                        HttpStatus.NOT_FOUND)))
                .flatMap(user -> jwtService
                        .saveRefreshedTokenPair(expiresAt, Instant.now(),
                                accessTokenExpiration, refreshTokenExpiration,
                                user)
                        .flatMap(tokensEntity -> {
                            try {
                                String accessToken = regenerateAccessTokenWithNewExpiration(decodedAccessToken, accessTokenExpiration);
                                String refreshToken = generateBasicToken(user, tokensEntity, TokenType.REFRESH, accessTokenExpiration);
                                return Mono.just(new TokenPair(
                                        new AccessToken(accessToken),
                                        new RefreshToken(refreshToken)));
                            } catch (JWTCreationException exception) {
                                return Mono.error(new BasicException(
                                        Map.of("jwt", "Error occurred while refreshing tokens"),
                                        HttpStatus.BAD_REQUEST));
                            }
                        }));
    }

    @Transactional
    public Mono<TokenPair> generateTokens(UserEntity user) {
        Instant accessTokenExpiration = calculateExpirationInstantWithMicros(accessTokenDurationInSeconds);
        Instant refreshTokenExpiration = calculateExpirationInstantWithMicros(refreshTokenDurationInSeconds);

        return jwtService.saveGeneratedTokenPair(Instant.now(), accessTokenExpiration, refreshTokenExpiration, user)
                .flatMap(tokensEntity -> {
                    try {
                        String accessToken = generateBasicToken(user, tokensEntity, TokenType.ACCESS, accessTokenExpiration);
                        String refreshToken = generateBasicToken(user, tokensEntity, TokenType.REFRESH, refreshTokenExpiration);
                        return Mono.just(new TokenPair(
                                new AccessToken(accessToken),
                                new RefreshToken(refreshToken)));
                    } catch (JWTCreationException exception) {
                        return Mono.error(new BasicException(
                                Map.of("jwt", "Error occurred while generating tokens"),
                                HttpStatus.BAD_REQUEST
                        ));
                    }
                });
    }

    public String generateTokenWithNewClaims(Map<String, Object> newClaims, DecodedJWT decodedJWT) {
        JWTCreator.Builder jwtBuilder = JWT.create();

        jwtBuilder.withSubject(decodedJWT.getSubject());
        newClaims.forEach((key, value) -> jwtBuilder.withClaim(key, value.toString()));
        jwtBuilder.withExpiresAt(decodedJWT.getExpiresAtAsInstant());

        return jwtBuilder.sign(algorithm);
    }

    public String regenerateAccessTokenWithNewExpiration(DecodedJWT decodedAccessToken, Instant newExpiration) {
        JWTCreator.Builder jwtBuilder = JWT.create();
        jwtBuilder.withSubject(decodedAccessToken.getSubject());
        decodedAccessToken.getClaims()
                .forEach((key, value) ->
                        jwtBuilder.withClaim(key, value.asString()));
        jwtBuilder.withExpiresAt(newExpiration);

        return jwtBuilder.sign(algorithm);
    }

    private String generateBasicToken(UserEntity user, TokensEntity tokensEntity, TokenType tokenType, Instant expiration) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withClaim(USER_ID, user.getId().toString())
                .withClaim(JWT_ID, tokensEntity.getRefreshToken().getId().toString())
                .withClaim(FAMILY_ID, tokensEntity.getId().toString())
                .withClaim(TOKEN_TYPE, tokenType.toString())
                .withClaim(PROVIDER, user.getProvider().toString().toUpperCase())
                .withClaim(ROLE, user.getRoles().toString())
                .withExpiresAt(expiration)
                .sign(algorithm);
    }

    private Instant calculateExpirationInstantWithMicros(long seconds) {
        return Instant.now().plusSeconds(seconds);
    }

    private boolean areTokensLinked(DecodedJWT decodedAccessToken, DecodedJWT decodedRefreshToken) {
        return decodedAccessToken.getSubject().equals(decodedRefreshToken.getSubject());
    }

    public String addClaimsIdToJwtToken(Map<String, Object> newClaims, String jwt) {
        try {
            DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(jwt);

            Map<String, Object> claims = decodedJWT.getClaims().entrySet().stream()
                    .collect(Collectors
                            .toMap(Map.Entry::getKey, entry ->
                                    entry.getValue().as(Object.class)));

            claims.putAll(newClaims);

            return JWT.create()
                    .withSubject(decodedJWT.getSubject())
                    .withPayload(claims)
                    .withExpiresAt(decodedJWT.getExpiresAt())
                    .sign(algorithm);
        } catch (JWTVerificationException | JWTCreationException exception) {
            throw new BasicException(
                    Map.of("jwt", "Error occurred while generating tokens. Error description: " + exception),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
