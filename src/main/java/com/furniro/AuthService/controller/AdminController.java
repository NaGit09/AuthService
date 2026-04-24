package com.furniro.AuthService.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.service.AdminService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/resetPassword")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> resetPassword(
            @RequestBody List<Integer> accountIDs) {
        return adminService.resetPassword(accountIDs);
    }

    @PostMapping("/ban")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> ban(
            @RequestBody List<Integer> accountIDs) {
        return adminService.banAccount(accountIDs);
    }

    @PostMapping("/unban")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> unban(
            @RequestBody List<Integer> accountIDs) {
        return adminService.unbanAccount(accountIDs);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> delete(
            @RequestBody List<Integer> accountIDs) {
        return adminService.deleteAccount(accountIDs);
    }

    @GetMapping("/all")
    public ResponseEntity<AType> getAllAccounts(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam String sortBy) {
        return adminService.getAllAccounts(page, size, sortBy);
    }

}
