package com.example.reactive.security.security.service.user;

import com.example.reactive.security.exceptions.BasicException;
import com.example.reactive.security.foruser.CustomUserPrincipal;
import com.example.reactive.security.foruser.UserEntity;
import com.example.reactive.security.foruser.UserService;
import com.example.reactive.security.security.constant.UrlBase;
import com.example.reactive.security.security.models.enums.Provider;
import com.example.reactive.security.security.service.email_sending.EmailSenderService;
import com.example.reactive.security.security.service.submission.SubmissionCodeCachingService;
import com.example.reactive.security.security.util.generate.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {
    private final ReactiveAuthenticationManager authenticationManager;
    private final UserService userService;
    private final EmailSenderService emailSenderService;
    private final SubmissionCodeCachingService submissionCodeCachingService;
    private final CodeGenerator codeGenerator;

    @Transactional
    public Mono<Void> createLocalUserAndSendSubmissionLink(String email, String password) {
        log.info("createLocalUserAndSendSubmissionLink: {}", email);
        return userService.existsByEmailAndProvider(email, Provider.LOCAL)
                .doOnError(error -> log.info("existsByEmailAndProvider: {}", error.getMessage()))
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new BasicException(
                        Map.of("email", "Email is already taken"),
                        HttpStatus.BAD_REQUEST)))
                .flatMap(_ -> userService.createLocalUser(email, password)
                        .doOnError(error -> log.info("createLocalUser: {}", error.getMessage()))
                        .and(generateThenCacheAndSendSubmissionCode(email)))
                .doOnError(error -> log.error("createLocalUserAndSendSubmissionLink: {}", error.getMessage()));
    }

    private Mono<Void> generateThenCacheAndSendSubmissionCode(String email) {
        return codeGenerator.generateCode().flatMap(code -> {
            Mono<Void> cacheCodeMono = submissionCodeCachingService
                    .createEmailSubmissionCodeWithExpiration(email, code);
            Mono<Void> sendLinkMono = emailSenderService
                    .sendSubmissionLink(email, code, UrlBase.VERIFY_LOCAL);
            return Mono.when(cacheCodeMono, sendLinkMono);
        });
    }

    @Transactional
    public Mono<Void> sendMailWithAccountSubmissionLink(String email) {
        return userService
                .findByEmailAndProvider(email, Provider.LOCAL)
                .switchIfEmpty(Mono.error(new BasicException(
                        Map.of("email", "User with such email not found."),
                        HttpStatus.NOT_FOUND)))
                .filter(user -> !user.isEmailSubmitted())
                .switchIfEmpty(Mono.error(new BasicException(
                        Map.of("email", "Email is already submitted"),
                        HttpStatus.BAD_REQUEST)))
                .flatMap(unused -> generateThenCacheAndSendSubmissionCode(email));
    }

    @Transactional
    public Mono<Void> verifyUserAccount(String code, String email) {
        return submissionCodeCachingService.isEmailSubmissionCodeExists(code, email)
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new BasicException(
                        Map.of("code", "Code is expired or not exists "),
                        HttpStatus.NOT_FOUND)))
                .flatMap(unused -> userService.updateIsEmailSubmittedByEmailAndProvider(email, Provider.LOCAL))
                .then();
    }

    @Transactional
    public Mono<Void> cachePasswordAndSendPasswordChangeSubmissionLink(String email, String password) {
        return userService.findByEmailAndProvider(email, Provider.LOCAL)
                .switchIfEmpty(Mono.error(new BasicException(
                        Map.of("email", "User with such email not found."),
                        HttpStatus.NOT_FOUND)))
                .flatMap(unused -> {
                    Mono<Void> cacheEmailAndPasswordMono = submissionCodeCachingService
                            .cacheEmailAndNewPasswordUntilSubmission(email, password);

                    Mono<Void> generateThenCacheAndSendCodeMono = generateThenCacheAndSendPasswordChangeCode(email);

                    return Mono.when(cacheEmailAndPasswordMono, generateThenCacheAndSendCodeMono);
                });
    }

    private Mono<Void> generateThenCacheAndSendPasswordChangeCode(String email) {
        return codeGenerator.generateCode()
                .flatMap(code -> {
                    Mono<Void> cacheCodeAndPasswordMono = submissionCodeCachingService
                            .createChangePasswordSubmissionCodeWithExpiration(email, code);
                    Mono<Void> sendLinkMono = emailSenderService
                            .sendSubmissionLink(email, code, UrlBase.SUBMIT_PASSWORD_CHANGE);
                    return Mono.when(cacheCodeAndPasswordMono, sendLinkMono);
                });
    }

    @Transactional
    public Mono<Void> submitPasswordChange(String email, String code) {
        return submissionCodeCachingService.isChangePasswordSubmissionCodeExists(code, email)
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new BasicException(
                        Map.of("code", "Code not found"),
                        HttpStatus.NOT_FOUND)))
                .flatMap(unused -> submissionCodeCachingService.popPasswordByEmail(email))
                .flatMap(newPassword -> userService.changePassword(email, newPassword));
    }

    @Transactional
    public Mono<UserEntity> authenticate(String email, String password, Provider provider) {
        CustomUserPrincipal customUserPrincipal = new CustomUserPrincipal(email, provider);
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(customUserPrincipal, password);
        return authenticationManager.authenticate(usernamePassword)
                .filter(Authentication::isAuthenticated)
                .switchIfEmpty(Mono.error(new BasicException(
                        Map.of("email_or_password", "Email or password isn't correct"),
                        HttpStatus.NOT_FOUND)))
                .map(authUser ->{
                    if (authUser instanceof CredentialsContainer container)
                        container.eraseCredentials();

                    return (UserEntity) authUser.getPrincipal();
                });
    }
}
