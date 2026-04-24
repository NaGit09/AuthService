package com.furniro.AuthService.service;

import java.util.List;
import java.util.function.IntSupplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.repository.AccountRepository;
import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ApiType;
import com.furniro.AuthService.exception.imp.AuthException;
import com.furniro.AuthService.util.error.AuthErrorCode;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j

public class AdminService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    private ResponseEntity<AType> executeBulkUpdate(
            List<Integer> accountIDs,
            String successMessage,
            IntSupplier updateLogic) {

        // 1. Flash check user in list exist
        long count = accountRepository.countByAccountIDIn(accountIDs);

        if (count == 0) {
            throw new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND);
        }

        // 2. Execute update logic
        int result = updateLogic.getAsInt();

        if (result == 0) {
            throw new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND);
        }

        // 3. Return data with ApiType format
        String message = successMessage + " for " + result + "/" + accountIDs.size() + " account";
        log.info("Message: {}", message);
        AType success = ApiType.builder()
                .code(200)
                .message(message)
                .data(true)
                .build();

        return ResponseEntity.ok(success);
    }

    public ResponseEntity<AType> resetPassword
        (@NotEmpty List<Integer> ids) {
        String hashPassword = passwordEncoder.encode("furniro2026");

        return executeBulkUpdate(
                ids,
                "Reset password",
                () -> accountRepository.resetPasswords(ids, hashPassword)
        );
    }

    public ResponseEntity<AType> banAccount
        (@NotEmpty List<Integer> ids) {
        return executeBulkUpdate(
                ids,
                "Ban account",
                () -> accountRepository.banAccounts(ids)
        );
    }

    public ResponseEntity<AType> unbanAccount
        (@NotEmpty List<Integer> ids) {
        return executeBulkUpdate(
                ids,
                "Unban account",
                () -> accountRepository.unbanAccounts(ids)
        );
    }

    public ResponseEntity<AType> deleteAccount
        (@NotEmpty List<Integer> ids) {
        return executeBulkUpdate(
                ids,
                "Delete account",
                () -> accountRepository.deleteAccounts(ids)
        );
    }

    public ResponseEntity<AType> getAllAccounts(
            Integer page,
            Integer size,
            String sortBy) {

        // 1. Create pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        // 2. Get all accounts
        Page<Account> getAccounts = accountRepository.findAll(pageable);

        if (getAccounts.isEmpty()) {
            throw new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND);
        }

        // 3. Return data with ApiType format
        AType success = ApiType.builder()
                .code(200)
                .message("Get all accounts successfully")
                .data(getAccounts)
                .build();

        return ResponseEntity.ok(success);
    }
}