package com.furniro.AuthService.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginReq {
    @NotBlank(message ="Email is not empty")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Password is not empty")
    private String password;
}
