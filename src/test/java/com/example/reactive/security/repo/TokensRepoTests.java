package com.example.reactive.security.repo;

import com.example.reactive.security.foruser.UserEntity;
import com.example.reactive.security.foruser.UserRepo;
import com.example.reactive.security.security.models.entities.tokens.AccessTokenEntity;
import com.example.reactive.security.security.models.entities.tokens.RefreshTokenEntity;
import com.example.reactive.security.security.models.entities.tokens.TokensEntity;
import com.example.reactive.security.security.models.enums.Provider;
import com.example.reactive.security.security.models.enums.UserRole;
import com.example.reactive.security.security.repo.TokensRepo;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Repeat;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest(
        classes = {TokensRepo.class, UserRepo.class},
        properties = {"spring.profiles.active=test"}
)
@ComponentScan(basePackages = {"com.example.reactive.security"})
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TokensRepoTests {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15-alpine"
    );
    @Autowired
    TokensRepo tokensRepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    PasswordEncoder encoder;
    private static String email = "<EMAIL>";
    private static int test_save = 0;
    private final String password = "<PASSWORD>";
    private static UUID tokens_id;
    private static UserEntity userEntity;

    @Order(1)
    @RepeatedTest(5)
    void test_save() {
        UserEntity userEntity = userRepo.save(UserEntity.builder()
                .email(email)
                .password(encoder.encode(password))
                .isEmailSubmitted(false)
                .registrationDate(LocalDateTime.now())
                .provider(Provider.LOCAL)
                .roles(UserRole.USER)
                .build()).block();

        this.userEntity = userEntity;
        Assertions.assertNotNull(userEntity);
        System.out.println("User Entity:" +  userEntity);

        TokensEntity tokensEntity = TokensEntity.builder()
                .accessToken(AccessTokenEntity.builder()
                        .expiresAt(Instant.now())
                        .build())
                .refreshToken(RefreshTokenEntity.builder()
                        .expiresAt(Instant.now())
                        .build())
                .users(userEntity)
                .updatedAt(Instant.now())
                .build();

        TokensEntity tokensEntityRes = tokensRepo.save(tokensEntity).block();
        System.out.println("TokensEntity: " + tokensEntityRes);
        Assertions.assertNotNull(tokensEntityRes);
        System.out.println("Tokens id: " + tokensEntityRes.getId());
        Assertions.assertNotNull(tokensEntityRes.getId());
        tokens_id = tokensEntityRes.getId();
        test_save++;
        email = email + test_save;
    }

    @Test
    @Order(2)
    void test_findById() {
        TokensEntity tokensEntityRes = tokensRepo.findById(tokens_id).block();
        System.out.printf("TokensEntity: %s\n", tokensEntityRes);
        Assertions.assertNotNull(tokensEntityRes);
    }
    @Test
    @Order(3)
    void test_finByUsers(){
        TokensEntity tokensEntityRes = tokensRepo.findByUsers(userEntity).block();
        System.out.printf("TokensEntity: %s\n", tokensEntityRes);
        Assertions.assertNotNull(tokensEntityRes);
    }
}


