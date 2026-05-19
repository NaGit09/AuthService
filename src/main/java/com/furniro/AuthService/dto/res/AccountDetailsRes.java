package com.furniro.AuthService.dto.res;

import com.furniro.AuthService.util.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDetailsRes {
    private Integer AccountID;
    private String UserName;
    private String Email;
    private String Phone;
    private Role Role;
    private Boolean Active;
    private Boolean Banned;
    private String FirstName;
    private String LastName;
    private String AvatarUrl;
}
