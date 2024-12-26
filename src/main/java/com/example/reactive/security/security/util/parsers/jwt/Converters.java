package com.example.reactive.security.security.util.parsers.jwt;

import com.auth0.jwt.interfaces.Claim;
import com.example.reactive.security.security.models.enums.Provider;
import com.example.reactive.security.security.models.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

import static com.example.reactive.security.security.util.parsers.ProviderEnumParser.getProviderFromString;
import static com.example.reactive.security.security.util.parsers.UserRolesParser.getUserRolesFromString;

class Converters {
    public static UUID asUUID( Claim claim) {
            return UUID.fromString(claim.asString());
    }
    public static String asString(Claim claim) {
        return claim.asString();
    }

    public static Instant asInstant(Claim claim){
        return claim.asInstant();
    }

    public static Provider asProvider(Claim claim){
        return getProviderFromString(claim.asString());
    }
    public static UserRole asUserRole(Claim claim){
        return getUserRolesFromString(claim.asString());
    }
}
