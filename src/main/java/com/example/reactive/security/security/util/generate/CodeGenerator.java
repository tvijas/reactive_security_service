package com.example.reactive.security.security.util.generate;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;

@Component
public final class CodeGenerator {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 16;
    private final SecureRandom random = new SecureRandom();


    public Mono<String> generateCode() {
        return Mono.fromCallable(() -> {
            StringBuilder code = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                int randomIndex = random.nextInt(CHARACTERS.length());
                char randomChar = CHARACTERS.charAt(randomIndex);
                code.append(randomChar);
            }
            return code.toString();
        });
    }
}
