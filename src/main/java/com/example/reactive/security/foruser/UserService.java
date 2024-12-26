package com.example.reactive.security.foruser;

import com.example.reactive.security.exceptions.BasicException;
import com.example.reactive.security.security.models.enums.Provider;
import com.example.reactive.security.security.models.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder encoder;

    @Transactional
    public Mono<Void> createLocalUser(String email, String password) {
        return userRepo.save(UserEntity.builder()
                        .email(email)
                        .password(encoder.encode(password))
                        .isEmailSubmitted(false)
                        .registrationDate(LocalDateTime.now())
                        .provider(Provider.LOCAL)
                        .roles(UserRole.USER)
                        .build())
                .doOnError(err -> log.error("Failed to create user: " + err.getMessage()))
                .onErrorResume(ex -> Mono.error(new BasicException(
                        Map.of("user", "Failed to create user"),
                        HttpStatus.INTERNAL_SERVER_ERROR
                )))
                .then();
    }

    public Mono<Boolean> existsByEmailAndProvider(String email, Provider provider) {
        return userRepo.existsByEmailAndProvider(email, provider);
    }

    public Mono<Void> updateIsEmailSubmittedByEmailAndProvider(String email, Provider provider) {
        return userRepo.updateIsEmailSubmittedByEmailAndProvider(email, provider)
                .flatMap(rowsUpdated -> rowsUpdated != 1
                        ? Mono.error(new BasicException(
                        Map.of("email", "User with such email not found"),
                        HttpStatus.NOT_FOUND))
                        : Mono.empty());
    }

    public Mono<UserEntity> findByEmailAndProvider(String email, Provider provider) {
        return userRepo.findByEmailAndProvider(email, provider);
    }

    @Transactional
    public Mono<Void> deleteUserById(UUID userId) {
        return userRepo.existsById(userId)
                .flatMap(exists -> exists
                        ? Mono.error(new BasicException(
                        Map.of("userId", "User with such id not found"), HttpStatus.NOT_FOUND))
                        : userRepo.deleteById(userId));
    }


    @Transactional
    public Mono<Void> changePassword(String email, String password) {
        return userRepo.findByEmailAndProvider(email, Provider.LOCAL)
                .switchIfEmpty(Mono.error(new BasicException(
                        Map.of("email", "User with such email not found"),
                        HttpStatus.NOT_FOUND)))
                .flatMap(user -> {
                    user.setPassword(encoder.encode(password));
                    return userRepo.save(user).then();
                });
    }
}