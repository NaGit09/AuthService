package com.furniro.AuthService.exception;

import com.furniro.AuthService.util.enums.AuthErrorCode;
import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    private final AuthErrorCode errorCode;

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
