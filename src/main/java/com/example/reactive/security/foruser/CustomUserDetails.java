package com.example.reactive.security.foruser;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;

public interface CustomUserDetails extends Serializable {
    Collection<? extends GrantedAuthority> getAuthorities();

    String getPassword();

    CustomUserPrincipal getPrincipal();

    default boolean isAccountNonExpired() {
        return true;
    }

    default boolean isAccountNonLocked() {
        return true;
    }

    default boolean isCredentialsNonExpired() {
        return true;
    }

    default boolean isEnabled() {
        return true;
    }
}
