package com.furniro.AuthService.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddAccountErrorRes {

    private Integer index;
    private String userName;
    private String reason;
}