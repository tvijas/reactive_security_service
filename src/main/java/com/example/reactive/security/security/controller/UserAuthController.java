package com.example.reactive.security.security.controller;

import com.example.reactive.security.exceptions.BasicException;
import com.example.reactive.security.security.models.enums.Provider;
import com.example.reactive.security.security.models.request.ChangePasswordRequest;
import com.example.reactive.security.security.models.request.LoginRequest;
import com.example.reactive.security.security.models.request.SignUpRequest;
import com.example.reactive.security.security.service.jwt.JwtGeneratorService;
import com.example.reactive.security.security.service.user.UserAuthService;
import com.example.reactive.security.security.util.annotations.validators.email.EmailExists;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.reactive.security.security.util.parsers.AuthHeaderParser.recoverToken;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserAuthController {
    private final UserAuthService userAuthService;
    private final JwtGeneratorService jwtGeneratorService;

    @PostMapping("/register")
// @WithRateLimitProtection
    public Mono<ResponseEntity<Void>> register(@RequestBody @Valid SignUpRequest request) {
        return userAuthService
                .createLocalUserAndSendSubmissionLink(request.getEmail(), request.getPassword())
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());
    }


    @PostMapping("/verify/local")
//    @WithRateLimitProtection
    public Mono<ResponseEntity<Void>> verifyEmail(@RequestParam String code, @RequestParam String email) {
        return userAuthService
                .verifyUserAccount(code, email)
                .thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/resend-submission-link")
//    @WithRateLimitProtection(rateLimit = 3, rateDuration = 180_000)
    public Mono<ResponseEntity<Void>> submitEmail(@RequestParam @Valid @Email @EmailExists String email) {
        return userAuthService
                .sendMailWithAccountSubmissionLink(email)
                .thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/login")
//    @WithRateLimitProtection
    public Mono<ResponseEntity<Void>> login(@RequestBody @Valid LoginRequest request) {
        return userAuthService.authenticate(request.getEmail(), request.getPassword(), Provider.LOCAL)
                .flatMap(jwtGeneratorService::generateTokens)
                .map(tokenPair -> ResponseEntity.ok()
                        .header("Authorization", "Bearer " + tokenPair.getAccessTokenValue())
                        .header("X-Refresh-Token", tokenPair.getRefreshTokenValue())
                        .build());
    }

    @PostMapping("/token/refresh")
    //    @WithRateLimitProtection
    public Mono<ResponseEntity<Void>> refreshTokens(@RequestHeader("X-Refresh-Token") String refreshToken,
                                              @RequestHeader("Authorization") String accessToken) {
        String parsedAccessToken = recoverToken(accessToken).orElseThrow(() ->
                new BasicException(Map.of("Authorization", "Authorization header is empty or has invalid format"), HttpStatus.BAD_REQUEST));

        return jwtGeneratorService
                .refreshTokens(parsedAccessToken, refreshToken)
                .map(tokenPair -> ResponseEntity.ok()
                        .header("Authorization", "Bearer " + tokenPair.getAccessTokenValue())
                        .header("X-Refresh-Token", tokenPair.getRefreshTokenValue())
                        .build());
    }

    @PostMapping("/change-password")
//    @WithRateLimitProtection
    public Mono<ResponseEntity<Void>> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        return userAuthService
                .cachePasswordAndSendPasswordChangeSubmissionLink(request.getEmail(), request.getPassword())
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).build());
    }

    @PostMapping("/submit-password-change")
//    @WithRateLimitProtection
    public Mono<ResponseEntity<Void>> submitPasswordChange(@RequestParam String code, @RequestParam String email) {
        return userAuthService.submitPasswordChange(email, code)
                .thenReturn(ResponseEntity.ok().build());
    }
}
