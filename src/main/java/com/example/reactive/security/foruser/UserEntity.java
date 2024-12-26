package com.example.reactive.security.foruser;

import com.example.reactive.security.security.models.enums.Provider;
import com.example.reactive.security.security.models.enums.UserRole;
import io.r2dbc.spi.Readable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "users")
public class UserEntity implements CustomUserDetails, Serializable {
    @Id
    private UUID id;
    private String email;
    private String password;
    private Provider provider;
    private String providerId;
    //    @Column("last_active_date")
    private LocalDateTime lastActiveDate;
    //    @Column("is_email_submitted")
    private LocalDateTime registrationDate;
    //    @Column("is_email_submitted")
    private boolean isEmailSubmitted;
    private UserRole roles;
//    @Column("tokens_id")
//    private UUID tokensId;
//    @PrePersist
//    public void prePersist() {
//        if (provider == null)
//            this.provider = Provider.LOCAL;
//        if(roles == null)
//            this.roles = UserRole.USER;
//    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.roles == UserRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public CustomUserPrincipal getPrincipal() {
        return new CustomUserPrincipal(this.email, this.provider);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isEmailSubmitted;
    }

    public static UserEntity fromRow(Readable row) {
        return UserEntity.builder()
                .id(row.get("user_id", UUID.class))
                .email(row.get("email", String.class))
                .password(row.get("password", String.class))
                .provider(Provider.valueOf(row.get("provider", String.class)))
                .providerId(row.get("provider_id", String.class))
                .lastActiveDate(row.get("last_active_date", LocalDateTime.class))
                .registrationDate(row.get("registration_date", LocalDateTime.class))
                .isEmailSubmitted(row.get("is_email_submitted", Boolean.class))
                .roles(UserRole.valueOf(row.get("roles", String.class)))
                .build();
    }

}