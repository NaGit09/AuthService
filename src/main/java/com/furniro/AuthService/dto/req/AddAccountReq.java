package com.furniro.AuthService.dto.req;

import com.furniro.AuthService.util.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAccountReq {

    @NotBlank(message = "Username is required")
    @Size(max = 50)
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank(message = "Password is required")
    private String password;

    private Role role;
}
