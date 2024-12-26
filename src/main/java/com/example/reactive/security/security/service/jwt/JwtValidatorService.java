package com.example.reactive.security.security.service.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.reactive.security.exceptions.BasicException;
import com.example.reactive.security.security.models.enums.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtValidatorService {
    private final JwtDecoderService jwtDecoderService;
    public Optional<DecodedJWT> validateToken(String token, TokenType expectedTokenType) {
        Optional<DecodedJWT> decodedJWT = jwtDecoderService.decodeJwt(token);

        if (decodedJWT.isEmpty()) return decodedJWT;

        checkTokenTypeOfDecodedJwt(expectedTokenType, decodedJWT.get());

        return decodedJWT;
    }

    public Optional<DecodedJWT> validateTokenWithoutExp(String token, TokenType expectedTokenType) {
        Optional<DecodedJWT> decodedJWT = jwtDecoderService.decodeJwtWithoutExp(token);

        if (decodedJWT.isEmpty()) return decodedJWT;

        checkTokenTypeOfDecodedJwt(expectedTokenType, decodedJWT.get());

        return decodedJWT;
    }

    public void checkTokenTypeOfDecodedJwt(TokenType expectedTokenType, DecodedJWT decodedJWT) {
        TokenType actualTokenType = TokenType.valueOf(decodedJWT.getClaim("tokenType").asString());

        if (!expectedTokenType.equals(actualTokenType))
            throw new BasicException(Map.of(
                    expectedTokenType.name().toLowerCase() + "_token", "Token type mismatch. " +
                            "Provided - " + actualTokenType +
                            " but expected - " + expectedTokenType),
                    HttpStatus.BAD_REQUEST);
    }
}
