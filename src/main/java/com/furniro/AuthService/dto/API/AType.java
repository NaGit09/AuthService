package com.furniro.AuthService.dto.API;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public abstract class AType {
    private int code;
    private String message;
}
