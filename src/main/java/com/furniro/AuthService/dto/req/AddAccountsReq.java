package com.furniro.AuthService.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddAccountsReq {

    @NotEmpty(message = "Account list must not be empty")
    @Size(max = 500, message = "Maximum 500 accounts per request")
    @Valid
    private List<AddAccountReq> accounts;
}