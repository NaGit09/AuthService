package com.furniro.AuthService.exception.imp;

import com.furniro.AuthService.exception.BaseException;
import com.furniro.AuthService.util.error.AddressErrorCode;

import lombok.Getter;

@Getter
public class AddressException extends BaseException {
    private final AddressErrorCode errorCode;

    public AddressException(AddressErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
