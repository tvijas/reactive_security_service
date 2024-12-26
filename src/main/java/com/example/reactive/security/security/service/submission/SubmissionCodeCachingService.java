package com.example.reactive.security.security.service.submission;

import com.example.reactive.security.exceptions.BasicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

import static com.example.reactive.security.security.constant.RedisPrefixes.*;
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionCodeCachingService {
    private final ReactiveStringRedisTemplate stringOps;

    public Mono<Void> createEmailSubmissionCodeWithExpiration(String email, String code) {
        return stringOps.opsForValue().set(EMAIL_SUBMISSION_CODE_PREFIX + code, email,
                Duration.ofMinutes(5)).then()
                .doOnError(err -> log.error("Failed to cache email and code: " + err.getMessage()))
                .onErrorResume(ex -> Mono.error(new BasicException(
                        Map.of("cache", "Failed to cache email and code"),
                        HttpStatus.INTERNAL_SERVER_ERROR)));
    }

    public Mono<Boolean> isEmailSubmissionCodeExists(String code, String email) {
        return stringOps.opsForValue().get(EMAIL_SUBMISSION_CODE_PREFIX + code)
                .map(value -> value != null && value.equals(email));

    }

    public Mono<Void> createChangePasswordSubmissionCodeWithExpiration(String email,String code) {
        return stringOps.opsForValue().set(PASSWORD_CHANGE_SUBMISSION_CODE_PREFIX + code, email,
                Duration.ofMinutes(5)).then()
                .doOnError(err -> log.error("Failed to cache email and code: " + err.getMessage()))
                .onErrorResume(ex -> Mono.error(new BasicException(
                        Map.of("cache", "Failed to cache email and code"),
                        HttpStatus.INTERNAL_SERVER_ERROR)));
    }

    public Mono<Void> cacheEmailAndNewPasswordUntilSubmission(String email, String password) {
        return stringOps.opsForValue().set(NEW_PASSWORD + email, password,
                Duration.ofMinutes(5)).then()
                .doOnError(err -> log.error("Failed to cache email and password until submission: " + err.getMessage()))
                .onErrorResume(ex -> Mono.error(new BasicException(
                        Map.of("cache", "Failed to cache email and password until submission"),
                        HttpStatus.INTERNAL_SERVER_ERROR)));
    }

    public Mono<Boolean> isChangePasswordSubmissionCodeExists(String code, String email) {
            return stringOps.opsForValue().getAndDelete(PASSWORD_CHANGE_SUBMISSION_CODE_PREFIX + code)
                    .map(value ->  value != null && value.equals(email));
    }

    public Mono<String> popPasswordByEmail(String email) {
        return stringOps.opsForValue().getAndDelete(NEW_PASSWORD + email);
    }
}