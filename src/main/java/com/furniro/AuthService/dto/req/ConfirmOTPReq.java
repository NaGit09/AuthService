package com.furniro.AuthService.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfirmOTPReq {

    @NotBlank(message ="Email is not empty")
    @Email(message = "Invalid email")
    private String email;
    
    @NotBlank(message ="OTP is not empty")
    @Size(min = 6, max = 6, message = "OTP must have 6 characters")
    private String otp;
}
