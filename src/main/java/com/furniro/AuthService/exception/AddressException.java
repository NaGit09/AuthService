package com.furniro.AuthService.exception;

import com.furniro.AuthService.util.enums.AddressErrorCode;
import lombok.Getter;

@Getter
public class AddressException extends RuntimeException {
    private final AddressErrorCode errorCode;

    public AddressException(AddressErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
