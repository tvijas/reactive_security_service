package com.example.reactive.security.security.util.annotations.validators.password;

import org.passay.*;

import java.util.List;

import static com.example.reactive.security.security.constant.Password.MAX_SIZE;
import static com.example.reactive.security.security.constant.Password.MIN_SIZE;


public class PasswordValidator {
    private final org.passay.PasswordValidator passwordValidator = new org.passay.PasswordValidator(List.of(
            new LengthRule(MIN_SIZE, MAX_SIZE),
            new RepeatCharactersRule(),
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            new CharacterRule(EnglishCharacterData.Digit, 1),
            new CharacterRule(CustomCharacterData.Special, 1),
            new AllowedRegexRule("^[a-zA-Z0-9@$!%*?&_-]+$")
    ));

    public boolean validate(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return passwordValidator.validate(new PasswordData(password)).isValid();
    }
}
