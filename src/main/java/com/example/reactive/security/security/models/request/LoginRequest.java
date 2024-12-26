package com.example.reactive.security.security.models.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email is blank")
    @Email(message = "Isn't email")
    @Size(max = 40, message = "Email is too long")
    private String email;
    @NotBlank(message = "Password is blank")
    private String password;
}
