package com.example.reactive.security.security.util.annotations.validators.password;

import org.passay.CharacterData;

import static com.example.reactive.security.security.constant.Password.SPECIAL_CHARS;


public enum CustomCharacterData implements CharacterData {
    Special("INSUFFICIENT_CUSTOM_SPECIAL",SPECIAL_CHARS);

    CustomCharacterData(String errorCode, String characters) {
        this.errorCode = errorCode;
        this.characters = characters;
    }

    private final String errorCode;
    private final String characters;

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getCharacters() {
        return characters;
    }
}
