package com.furniro.AuthService.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.req.ChangePasswordReq;
import com.furniro.AuthService.dto.req.ConfirmOTPReq;
import com.furniro.AuthService.dto.req.LoginReq;
import com.furniro.AuthService.dto.req.RegisterReq;
import com.furniro.AuthService.service.AccountService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public ResponseEntity<AType> register(
        @RequestBody RegisterReq registerReq) {
        return accountService.registerAccount(registerReq);
    }

    @PostMapping("/login")
    public ResponseEntity<AType> login
        (@RequestBody LoginReq loginReq) {
        return accountService.loginAccount(loginReq);
    }

    @PostMapping("/logout")
    public ResponseEntity<AType> logout(Authentication authentication) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        String token = jwtAuth.getToken().getTokenValue();
        return accountService.logoutAccount(token);
    }

    @PostMapping("/sendOTP")
    public ResponseEntity<AType> sendOPT
        (@RequestBody String email) {
        return accountService.sendOTP(email);
    }

    @PostMapping("/confirmOTP")
    public ResponseEntity<AType> confirmOTP
        (@RequestBody ConfirmOTPReq confirmOTPReq) {
        return accountService.confirmOTP(confirmOTPReq);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<AType> changePassword
        (@RequestBody ChangePasswordReq changePasswordReq) {
        return accountService.changePassword(changePasswordReq);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AType> refreshToken(
        @RequestHeader("Authorization") String token) {
        String refreshToken = token.substring(7);
        return accountService.refreshToken(refreshToken);
    }

    // ADMIN API
    @PostMapping("/resetPassword")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> resetPassword(
        @RequestBody List<Integer> accountIDs) {
        return accountService.resetPassword(accountIDs);
    }

    @PostMapping("/ban")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> ban(
        @RequestBody List<Integer> accountIDs) {
        return accountService.banAccount(accountIDs);
    }

    @PostMapping("/unban")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> unban(
        @RequestBody List<Integer> accountIDs) {
        return accountService.unbanAccount(accountIDs);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> delete(
        @RequestBody List<Integer> accountIDs) {
        return accountService.deleteAccount(accountIDs);
    }
}
