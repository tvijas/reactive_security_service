package com.example.reactive.security.security.util.parsers.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.reactive.security.exceptions.BasicException;
import com.example.reactive.security.foruser.CustomUserDetails;
import com.example.reactive.security.foruser.UserEntity;
import com.example.reactive.security.security.models.enums.Provider;
import com.example.reactive.security.security.models.enums.UserRole;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static com.example.reactive.security.security.constant.JwtClaimKey.*;


public final class JwtPayloadParser {
    private JwtPayloadParser() {
    }

    public static <T> T getClaimValueByKey(Map<String, Claim> claims, String key, Function<Claim, T> converter) {
        if (claims == null)
            throw new BasicException(Map.of(key, "Claim is null"), HttpStatus.BAD_REQUEST);

        Claim claim = claims.get(key);
        if (claim == null || claim.isMissing())
            throw new BasicException(Map.of(key, "There is no such key in token claims"), HttpStatus.BAD_REQUEST);

        if (claim.isNull())
            throw new BasicException(Map.of(key, "Key exists but value is null"), HttpStatus.BAD_REQUEST);

        try {
            return converter.apply(claim);
        } catch (Exception ex) {
            throw new BasicException(Map.of(key, "Claim can not be parsed. Description: " + ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    public static UUID getIdFromClaimsByKey(Map<String, Claim> claims, String key) {
        return getClaimValueByKey(claims, key, Converters::asUUID);
    }

    public static Instant getExpiresAt(Map<String, Claim> claims) {
        return getClaimValueByKey(claims, EXPIRES_AT, Converters::asInstant);
    }

    public static Provider getProviderFromClaims(Map<String, Claim> claims) {
        return getClaimValueByKey(claims, PROVIDER, Converters::asProvider);
    }

    public static UserRole getUserRoleFromClaims(Map<String, Claim> claims) {
        return getClaimValueByKey(claims, ROLE, Converters::asUserRole);
    }
    public static CustomUserDetails getUserDetailsFromClaims(Map<String, Claim> claims){
        return UserEntity.builder()
                .email(getClaimValueByKey(claims,EMAIL, Converters::asString))
                .provider(getProviderFromClaims(claims))
                .roles(getUserRoleFromClaims(claims))
                .build();
    }

    public static Map<String, Claim> parsePayloadFromJwt(String jwt) {
        DecodedJWT decodedJWT = JWT.decode(jwt);
        return parsePayloadFromDecodedJwt(decodedJWT);
    }

    public static Map<String, Claim> parsePayloadFromDecodedJwt(DecodedJWT decodedJWT) {
        Map<String, Claim> claims = new HashMap<>(decodedJWT.getClaims());
        claims.put("expiresAt", new InstantClaim(decodedJWT.getExpiresAtAsInstant()));
        claims.put("email", new StringClaim(decodedJWT.getSubject()));
        return claims;
    }
}
