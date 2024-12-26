package com.example.reactive.security.security.models.request;

import com.example.reactive.security.security.util.annotations.validators.email.EmailExists;
import com.example.reactive.security.security.util.annotations.validators.password.SecurePassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignUpRequest {
    @NotBlank(message = "Email is blank")
    @Email(message = "Isn't email")
    @Size(max = 40, message = "Email is too long")
    @EmailExists
    private final String email;
    @SecurePassword
    private final String password;
}
