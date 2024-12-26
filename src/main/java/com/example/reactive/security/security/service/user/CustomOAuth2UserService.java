package com.example.reactive.security.security.service.user;

import com.example.reactive.security.foruser.UserEntity;
import com.example.reactive.security.foruser.UserInfo;
import com.example.reactive.security.foruser.UserRepo;
import com.example.reactive.security.security.models.CustomOAuth2User;
import com.example.reactive.security.security.models.enums.Provider;
import com.example.reactive.security.security.models.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultReactiveOAuth2UserService {
    private final UserRepo usersRepo;

    @Override
    public void setWebClient(WebClient webClient) {
        super.setWebClient(webClient);
    }

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        return super.loadUser(userRequest)
                .flatMap(oAuth2User -> extractUserInfo(userRequest, oAuth2User)
                        .flatMap(userInfo -> processUser(userInfo, oAuth2User)));
    }

    private Mono<UserInfo> extractUserInfo(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        return Mono.fromCallable(() -> {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();

            return switch (registrationId) {
                case "google" -> new UserInfo(
                        oAuth2User.getAttribute("email"),
                        oAuth2User.getAttribute("sub"),
                        Provider.GOOGLE
                );
                default -> throw new OAuth2AuthenticationException(
                        "Unsupported provider: " + registrationId
                );
            };
        });
    }

    private Mono<OAuth2User> processUser(UserInfo userInfo, OAuth2User oAuth2User) {
        return usersRepo.findByProviderIdAndProvider(userInfo.providerId(), userInfo.provider())
                .switchIfEmpty(createNewUser(userInfo))
                .flatMap(existingUser -> updateExistingUser(existingUser, userInfo))
                .map(user -> new CustomOAuth2User(user, oAuth2User.getAttributes()));
    }

    private Mono<UserEntity> updateExistingUser(UserEntity existingUser, UserInfo userInfo) {
        return Mono.justOrEmpty(userInfo.email())
                .filter(email -> !email.equals(existingUser.getEmail()))
                .flatMap(newEmail -> {
                    existingUser.setEmail(newEmail);
                    return usersRepo.save(existingUser);
                })
                .defaultIfEmpty(existingUser);
    }

    private Mono<UserEntity> createNewUser(UserInfo userInfo) {
        return usersRepo.save(UserEntity.builder()
                .email(userInfo.email())
                .provider(userInfo.provider())
                .providerId(userInfo.providerId())
                .registrationDate(LocalDateTime.now())
                .isEmailSubmitted(true)
                .roles(UserRole.USER)
                .build());
    }

}