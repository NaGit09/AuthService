package com.furniro.AuthService.util.enums;

import lombok.Getter;

@Getter
public enum AddressErrorCode {
    ADDRESS_NOT_FOUND(404, "Address not exists"),
    ADDRESS_ALREADY_EXISTS(409, "Address has already exists"),
    ADDRESS_CREATE_FAILED(404, "Address create failed"),
    ADDRESS_UPDATE_FAILED(404, "Address update failed"),
    ADDRESS_DELETE_FAILED(404, "Address delete failed");

    private final int code;
    private final String message;

    AddressErrorCode(int status, String message) {
        this.code = status;
        this.message = message;
    }

}
