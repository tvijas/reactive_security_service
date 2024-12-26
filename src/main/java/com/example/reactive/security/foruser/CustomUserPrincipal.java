package com.example.reactive.security.foruser;


import com.example.reactive.security.security.models.enums.Provider;

public record CustomUserPrincipal(String email, Provider provider) {
}
