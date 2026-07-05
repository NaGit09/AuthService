package com.furniro.AuthService.controller;

import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.req.AddAccountReq;
import com.furniro.AuthService.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/add-accounts")
    @PreAuthorize("hasAuthority('ADMIN')")
    public AType addAccounts(@Valid @RequestBody List<AddAccountReq> request) {
    return adminService.addAccounts(request);
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
