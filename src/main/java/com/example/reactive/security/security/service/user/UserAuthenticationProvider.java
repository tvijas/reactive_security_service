package com.example.reactive.security.security.service.user;

import com.example.reactive.security.exceptions.BasicException;
import com.example.reactive.security.foruser.CustomUserPrincipal;
import com.example.reactive.security.foruser.UserEntity;
import com.example.reactive.security.foruser.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserAuthenticationProvider implements ReactiveAuthenticationManager {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        return userService.findByEmailAndProvider(userPrincipal.email(), userPrincipal.provider())
                .switchIfEmpty(Mono.error(new BasicException(
                        Map.of("email_or_password", "Email or password isn't correct"),
                        HttpStatus.BAD_REQUEST
                )))
                .flatMap(user -> Mono.just(user)
                        .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                        .switchIfEmpty(Mono.error(new BasicException(
                                Map.of("email_or_password", "Email or password isn't correct"),
                                HttpStatus.BAD_REQUEST
                        )))
                        .filter(UserEntity::isEnabled)
                        .switchIfEmpty(Mono.error(new BasicException(
                                Map.of("email", "Email is not verified"),
                                HttpStatus.BAD_REQUEST
                        )))
                        .map(u -> new UsernamePasswordAuthenticationToken(
                                u,
                                password,
                                u.getAuthorities()
                        ))).cast(Authentication.class);
    }
}

