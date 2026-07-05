package com.furniro.AuthService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ErrorType;
import com.furniro.AuthService.dto.req.ChangePasswordReq;
import com.furniro.AuthService.dto.req.ConfirmOTPReq;
import com.furniro.AuthService.dto.req.LoginByUsernameReq;
import com.furniro.AuthService.dto.req.LoginReq;
import com.furniro.AuthService.dto.req.LogoutReq;
import com.furniro.AuthService.dto.req.RegisterReq;
import com.furniro.AuthService.service.AccountService;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<AType> register(
            @RequestBody RegisterReq registerReq) {
        return accountService.registerAccount(registerReq);
    }

    @GetMapping("/active")
    public ResponseEntity<AType> activeAccount(
            @RequestParam("id") Integer accountID) {
        return accountService.activeAccount(accountID);
    }

    @PostMapping("/login")
    public ResponseEntity<AType> login(@RequestBody LoginReq loginReq) {
        return accountService.loginByEmail(loginReq);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<AType> sendOTP(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        return accountService.sendOTP(email);
    }

    @PostMapping("/confirm-otp")
    public ResponseEntity<AType> confirmOTP(@RequestBody ConfirmOTPReq confirmOTPReq) {
        return accountService.confirmOTP(confirmOTPReq);
    }

    @PostMapping("/change-password")
    public ResponseEntity<AType> changePassword(@RequestBody ChangePasswordReq changePasswordReq) {
        return accountService.changePassword(changePasswordReq);
    }

    // API require bearer token
    @PostMapping("/logout")
    public ResponseEntity<AType> logout(

        Authentication authentication,
        @RequestBody @Valid LogoutReq logoutReq) {

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {

            return ResponseEntity.status(401)
                    .body(ErrorType.unauthorized("Unauthorized"));
        }

        String accessToken = jwtAuth.getToken().getTokenValue();

        return accountService.logoutAccount(accessToken, logoutReq.getRefreshToken());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AType> refreshToken(

            Authentication authentication) {

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {

            return ResponseEntity.status(401)
                    .body(ErrorType.unauthorized("Unauthorized"));
        }

        String refreshToken = jwtAuth.getToken().getTokenValue();

        return accountService.refreshToken(refreshToken);
    }
}
