package com.furniro.AuthService.util.enums;

import lombok.Getter;

@Getter
public enum AuthErrorCode {
    INVALID_TOKEN(401, "Invalid token"),
    TOKEN_EXPIRED(401, "Your session has expired"),
    INVALID_PASSWORD(400, "Incorrect password"),
    EMAIL_ALREADY_EXISTS(409, "Email has already exists"),
    INVALID_ACTIVE_TOKEN(401, "Invalid active account token"),
    ACCOUNT_NOT_ACTIVE(404, "User don't active account"),
    ACCOUNT_IS_BANED(404, "User is banned"),
    NOT_FOUND_OTP(404, "Your OTP is expired or not exists"),
    OTP_NOT_MATCH(401, "Incorrect OTP"),
    OTP_EXPIRED(401, "Your OTP has expired"),
    PASSWORD_NOT_MATCH(401, "Incorrect password"),
    VERIFY_FAILED(401, "User vifies an error"),
    ACCOUNT_NOT_FOUND(404, "Account not found");

    private final int code;
    private final String message;

    AuthErrorCode(int status, String message) {
        this.code = status;
        this.message = message;
    }

}