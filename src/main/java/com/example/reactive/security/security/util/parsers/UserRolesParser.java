package com.example.reactive.security.security.util.parsers;

import com.example.reactive.security.exceptions.BasicException;
import com.example.reactive.security.security.models.enums.UserRole;
import org.springframework.http.HttpStatus;

import java.util.Map;

public final class UserRolesParser {
    private UserRolesParser() {
    }

    public static UserRole getUserRolesFromString(String roleStr) {
        try {
            return UserRole.valueOf(roleStr);
        } catch (IllegalArgumentException ex) {
            throw new BasicException(Map.of("roleStr", "User role has invalid format and cannot be parsed"), HttpStatus.BAD_REQUEST);
        }
    }
}
