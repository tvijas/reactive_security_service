package com.example.reactive.security.security.models.tokens;

public record TokenPair(
        AccessToken accessToken,
        RefreshToken refreshToken
) {
    public String getAccessTokenValue(){
        return this.accessToken.accessToken();
    }
    public String getRefreshTokenValue(){
        return this.refreshToken.refreshToken();
    }
}
