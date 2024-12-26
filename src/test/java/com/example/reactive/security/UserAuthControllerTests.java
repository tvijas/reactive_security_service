package com.example.reactive.security;

import com.example.reactive.security.foruser.UserRepo;
import com.example.reactive.security.security.models.enums.Provider;
import com.example.reactive.security.security.models.request.LoginRequest;
import com.example.reactive.security.security.models.request.SignUpRequest;
import com.example.reactive.security.security.util.generate.CodeGenerator;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(
        classes = {Application.class},
        properties = {"spring.profiles.active=test"}

)
//@ImportTestcontainers({RedisContainer.class, PostgreSQLContainer.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAuthControllerTests extends TestContainersInitializer {
    //    @Value("${spring.mail.username}")
    private String email = "tvijaseve@gmail.com";
    private static final String password = "fsfsDSF@545AADFDGEWE3AR";
    private static final String code = "STRING_PIZDATIY_CODE";
    private static final String newPassword = "!newPassword12345";
    private static String authHeader;
    private static String refreshHeader;
    @Autowired
    private UserRepo userRepo;
    @MockBean
    private CodeGenerator codeGenerator;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    WebTestClient webTestClient;

    @Test
    @Order(1)
    void test_register() {
        Mockito.when(this.codeGenerator.generateCode()).thenReturn(Mono.just(code));
        SignUpRequest signUpRequest = new SignUpRequest(email, password);

        webTestClient.post().uri("/api/user/register")
                .bodyValue(signUpRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().isEmpty();

        assertTrue(userRepo.findByEmailAndProvider(email, Provider.LOCAL).blockOptional().isPresent());
    }

    @Test
    @Order(2)
    void test_verify() {
        webTestClient.post().uri(uriBuilder -> uriBuilder
                        .path("/api/user/verify/local")
                        .queryParam("email", email)
                        .queryParam("code", code)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        assertTrue(userRepo.findByEmailAndProvider(email, Provider.LOCAL).block().isEmailSubmitted());
    }

    @Test
    @Order(3)
    void test_login() {
        LoginRequest loginRequest = new LoginRequest(email, password);

        EntityExchangeResult<Void> exchangeResult = webTestClient.post().uri("/api/user/login")
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Authorization")
                .expectHeader().exists("X-Refresh-Token")
                .expectBody().isEmpty();

        HttpHeaders headers = exchangeResult.getResponseHeaders();

        authHeader = headers.getFirst("Authorization");
        refreshHeader = headers.getFirst("X-Refresh-Token");

        assertFalse(authHeader.isEmpty());
        assertFalse(authHeader.isBlank());
        assertTrue(authHeader.startsWith("Bearer "));

        assertFalse(refreshHeader.isEmpty());
        assertFalse(refreshHeader.isBlank());
    }

    @Test
    @Order(4)
    void test_filter() {
        webTestClient.post().uri("/testing")
                .header("Authorization", authHeader)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    @Order(5)
    void test_change_password() {
        Mockito.when(this.codeGenerator.generateCode()).thenReturn(Mono.just(code));

        webTestClient.post().uri("/api/user/change-password")
                .header("Authorization", authHeader)
                .bodyValue(new SignUpRequest(email, newPassword))
                .exchange()
                .expectStatus().isAccepted()
                .expectBody().isEmpty();
    }

    @Test
    @Order(6)
    void test_submit_password_change() {
        webTestClient.post().uri(uriBuilder -> uriBuilder
                        .path("/api/user/submit-password-change")
                        .queryParam("email", email)
                        .queryParam("code", code)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        assertTrue(encoder.matches(newPassword, userRepo
                .findByEmailAndProvider(email, Provider.LOCAL).block().getPassword()));
    }
}
