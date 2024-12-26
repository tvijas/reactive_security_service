package com.example.reactive.security.repo;

import com.example.reactive.security.foruser.UserEntity;
import com.example.reactive.security.foruser.UserRepo;
import com.example.reactive.security.security.models.enums.Provider;
import com.example.reactive.security.security.models.enums.UserRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

@SpringBootTest(
        classes = {UserRepo.class},
        properties = {"spring.profiles.active=test"}
)
@ComponentScan(basePackages = {"com.example.reactive.security"})
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepoTests {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15-alpine"
    );
    @Autowired
    UserRepo userRepo;
    @Autowired
    PasswordEncoder encoder;
    private final String email = "<EMAIL>";
    private final String password = "<PASSWORD>";

    @Test
    @Order(1)
    void test_save() {
        UserEntity userEntity = userRepo.save(UserEntity.builder()
                .email(email)
                .password(encoder.encode(password))
                .isEmailSubmitted(false)
                .registrationDate(LocalDateTime.now())
                .provider(Provider.LOCAL)
                .roles(UserRole.USER)
                .build()).block();
        Assertions.assertNotNull(userEntity);
        System.out.printf("UserEntity: %s\n", userEntity);
    }

    @Test
    @Order(2)
    void test_existsByEmailAndProvider() {
        Boolean isTrue = userRepo.existsByEmailAndProvider(email, Provider.LOCAL).block();
        Assertions.assertEquals(Boolean.TRUE, isTrue);
    }
    @Test
    @Order(3)
    void test_findByEmailAndProvider(){
        UserEntity userEntity = userRepo.findByEmailAndProvider(email, Provider.LOCAL).block();
        Assertions.assertNotNull(userEntity);
        System.out.printf("UserEntity: %s\n", userEntity);
    }
    @Test
    @Order(4)
    void test_updateIsEmailSubmittedByEmailAndProvider(){
        Integer integer = userRepo.updateIsEmailSubmittedByEmailAndProvider(email, Provider.LOCAL).block();
        Assertions.assertEquals(1, integer);
    }
}
