package com.furniro.AuthService.dto.res;

import com.furniro.AuthService.util.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountRes { private Integer accountID;
    private String userName;
    private String email;
    private String phone;
    private Role role;
    private Boolean active;
    private Boolean banned;
}
