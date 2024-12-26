package com.example.reactive.security.security.util.annotations.validators.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.example.reactive.security.security.constant.Password.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SecurePasswordValidator.class)
public @interface SecurePassword {
    String message() default "Password must be between " + MIN_SIZE + " and " + MAX_SIZE + " characters long," +
            " contain at least 1 lowercase letter, 1 uppercase letter, 1 digit," +
            " 1 special character ("+SPECIAL_CHARS+")," +
            " have no repeated consecutive characters or whitespace, and no other characters.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
