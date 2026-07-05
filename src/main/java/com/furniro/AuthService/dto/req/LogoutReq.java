package com.furniro.AuthService.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutReq {

    @NotBlank(message = "Refresh token is not empty")
    private String refreshToken;
}
