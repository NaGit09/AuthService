package com.furniro.AuthService.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AddAccountsRes {

    private Integer total;
    private Integer successCount;
    private Integer failedCount;

    private List<AccountRes> successAccounts;
    private List<AddAccountErrorRes> errors;
}