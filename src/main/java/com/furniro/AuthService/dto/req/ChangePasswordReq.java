package com.furniro.AuthService.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordReq {
    @NotBlank(message = "Email cannot be blank")

    @Email(message = "Invalid email")

    private String email;

    @NotBlank(message = "Password cannot be blank")

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")

    private String password;

    @NotBlank(message = "Password confirmation cannot be blank")

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")

    private String confirmPassword;

}
