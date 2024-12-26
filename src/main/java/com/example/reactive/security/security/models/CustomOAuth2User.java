package com.example.reactive.security.security.models;

import com.example.reactive.security.foruser.UserEntity;
import com.example.reactive.security.security.models.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {
    private UserEntity user;
    private Map<String, Object> attributes;
    public CustomOAuth2User(UserEntity user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.user.getRoles() == UserRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    @Override
    public String getName() {
        return user.getEmail();
    }

    public UserEntity getUser() {
        return user;
    }
}

