package com.furniro.AuthService.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.furniro.AuthService.util.enums.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRes {
    @JsonProperty("AccessToken")
    private String AccessToken;

    @JsonProperty("RefreshToken")
    private String RefreshToken;

    @JsonProperty("FirstName")
    private String FirstName;

    @JsonProperty("LastName")
    private String LastName;

    @JsonProperty("UserName")
    private String UserName;

    @JsonProperty("AvatarUrl")
    private String AvatarUrl;

    @JsonProperty("Email")
    private String Email;

    @JsonProperty("Role")
    private Role Role;

    @JsonProperty("accountID")
    private Integer accountID; 
}
