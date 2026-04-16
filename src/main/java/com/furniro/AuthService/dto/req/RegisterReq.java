package com.furniro.AuthService.dto.req;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterReq {
    
    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 50, message = "Last name is maximum 50 characters")
    private String firstName;

    @NotBlank(message = "First name cannot be blank")
    @Size(max = 50, message = "First name is maximum 50 characters")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email is invalid")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^0[3-9]\\d{8}$", message = "Invalid phone number")
    private String numberPhone;
}
