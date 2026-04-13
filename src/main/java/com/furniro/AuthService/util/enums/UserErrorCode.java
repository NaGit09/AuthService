package com.furniro.AuthService.util.enums;

import lombok.Getter;

@Getter
public enum UserErrorCode {
    USER_NOT_FOUND(404, "User not exists"),
    USER_ALREADY_EXISTS(409, "User has already exists"),
    USER_CREATE_FAILED(404, "User create failed"),
    USER_UPDATE_FAILED(404, "User update failed"),
    USER_DELETE_FAILED(404, "User delete failed");

    private final int code;
    private final String message;

    UserErrorCode(int status, String message) {
        this.code = status;
        this.message = message;
    }

}
