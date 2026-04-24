package com.furniro.AuthService.dto.res;

import com.furniro.AuthService.util.enums.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRes {
    private String AccessToken;
    private String RefreshToken;
    private String FirstName;
    private String LastName;
    private String UserName;
    private String AvatarUrl;
    private String Email;
    private Role Role;
}
