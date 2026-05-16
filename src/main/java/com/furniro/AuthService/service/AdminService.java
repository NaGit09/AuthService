package com.furniro.AuthService.service;

import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.repository.AccountRepository;
import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ApiType;
import com.furniro.AuthService.dto.API.ErrorType;
import com.furniro.AuthService.dto.req.AddAccountReq;
import com.furniro.AuthService.dto.res.AccountRes;
import com.furniro.AuthService.exception.CustomException;
import com.furniro.AuthService.util.enums.LoginType;
import com.furniro.AuthService.util.enums.Role;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.function.IntSupplier;

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
            throw new CustomException(ErrorType.notFound("Account not found !"));
        }

        // 2. Execute update logic
        int result = updateLogic.getAsInt();

        if (result == 0) {
            throw new CustomException(ErrorType.notFound("Account not found !"));
        }

        // 3. Return data with ApiType format
        String message = successMessage + " for " + result + "/" + accountIDs.size() + " account";
        log.info("Message: {}", message);
        return ResponseEntity.ok(ApiType.success(true, message));
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
            throw new CustomException(ErrorType.notFound("Account not found !"));
        }

        // 3. Return data with ApiType format
        return ResponseEntity.ok(ApiType.success(getAccounts, "Get all accounts successfully"));
    }

    public ResponseEntity<AType> addAccount(AddAccountReq addAccountReq) {

        if (accountRepository.existsByEmail(addAccountReq.getEmail())) {
            throw new CustomException(ErrorType.badRequest("Email already exists !"));
        }

        if (accountRepository.existsByUserName(addAccountReq.getUserName())) {
            throw new CustomException(ErrorType.badRequest("Username already exists !"));
        }
        String passwordHash = passwordEncoder.encode(addAccountReq.getPassword());

        Account account = Account.builder()
                .userName(addAccountReq.getUserName())
                .email(addAccountReq.getEmail())
                .phone(addAccountReq.getPhone())
                .passwordHash(passwordHash)
                .loginType(LoginType.NORMAL)
                .role(addAccountReq.getRole() != null ? addAccountReq.getRole() : Role.CUSTOMER)
                .active(true)
                .banned(false)
                .isDeleted(false)
                .build();
        Account savedAccount = accountRepository.save(account);
        log.info("Add account successfully: accountID={}, userName={}",
                savedAccount.getAccountID(),
                savedAccount.getUserName()
        );
        AccountRes response = AccountRes.builder()
                .accountID(savedAccount.getAccountID())
                .userName(savedAccount.getUserName())
                .email(savedAccount.getEmail())
                .phone(savedAccount.getPhone())
                .role(savedAccount.getRole())
                .active(savedAccount.getActive())
                .banned(savedAccount.getBanned())
                .build();


        return ResponseEntity.ok(ApiType.success(response, "Add account successfully"));


    }
}