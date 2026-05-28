package com.furniro.AuthService.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginByUsernameReq {

    @NotBlank(message = "Username is not empty")
    private String userName;

    @NotBlank(message = "Password is not empty")
    private String password;
}