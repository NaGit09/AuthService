package com.furniro.AuthService.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.service.AdminService;

import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/reset-password")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> resetPassword(
            @RequestBody List<Integer> accountIDs) {
        return adminService.resetPassword(accountIDs);
    }

    @PostMapping("/ban-account")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> ban(
            @RequestBody List<Integer> accountIDs) {
        return adminService.banAccount(accountIDs);
    }

    @PostMapping("/unban-account")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> unban(
            @RequestBody List<Integer> accountIDs) {
        return adminService.unbanAccount(accountIDs);
    }

    @PostMapping("/delete-account")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> delete(
            @RequestBody List<Integer> accountIDs) {
        return adminService.deleteAccount(accountIDs);
    }

    @GetMapping("/all-account")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> getAllAccounts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        return adminService.getAllAccounts(page, size, sortBy);
    }

}
