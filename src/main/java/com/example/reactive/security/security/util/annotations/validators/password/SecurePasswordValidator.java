package com.example.reactive.security.security.util.annotations.validators.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SecurePasswordValidator implements ConstraintValidator<SecurePassword, String> {
    private final PasswordValidator passwordValidator = new PasswordValidator();

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return passwordValidator.validate(password);
    }
}
