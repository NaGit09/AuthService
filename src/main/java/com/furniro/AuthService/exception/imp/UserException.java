package com.furniro.AuthService.exception.imp;

import com.furniro.AuthService.exception.BaseException;
import com.furniro.AuthService.util.error.UserErrorCode;

import lombok.Getter;

@Getter
public class UserException extends BaseException {
    private final UserErrorCode errorCode;

    public UserException(UserErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
