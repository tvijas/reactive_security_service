package com.example.reactive.security.security.constant;

public final class RedisPrefixes {
    private RedisPrefixes() {}
    public static final String EMAIL_SUBMISSION_CODE_PREFIX = "submission_code:";
    public static final String PASSWORD_CHANGE_SUBMISSION_CODE_PREFIX = "change_password_submission_code:";
    public static final String NEW_PASSWORD = "new_password:";
}
