package com.example.reactive.security.foruser;

import com.example.reactive.security.security.models.enums.Provider;

public record UserInfo(String email, String providerId, Provider provider) {}
