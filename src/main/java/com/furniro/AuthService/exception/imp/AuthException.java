package com.furniro.AuthService.exception.imp;

import com.furniro.AuthService.exception.BaseException;
import com.furniro.AuthService.util.error.AuthErrorCode;

import lombok.Getter;

@Getter
public class AuthException extends BaseException {
    private final AuthErrorCode errorCode;

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
