package com.furniro.AuthService.service;

import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.repository.AccountRepository;
import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ApiType;
import com.furniro.AuthService.dto.API.ErrorType;
import com.furniro.AuthService.dto.req.AddAccountReq;
import com.furniro.AuthService.dto.req.AddAccountsReq;
import com.furniro.AuthService.dto.res.AccountDetailsRes;
import com.furniro.AuthService.dto.res.AccountRes;
import com.furniro.AuthService.dto.res.AddAccountErrorRes;
import com.furniro.AuthService.dto.res.AddAccountsRes;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
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

    @Transactional
    public ResponseEntity<AType> deleteAccount
            (@NotEmpty List<Integer> ids) {
        List<Account> accounts = accountRepository.findAllById(ids);
        if (accounts.isEmpty()) {
            throw new CustomException(ErrorType.notFound("Account not found !"));
        }

        accountRepository.deleteAll(accounts);

        String message = "Delete account for " + accounts.size() + "/" + ids.size() + " account";
        log.info("Message: {}", message);
        return ResponseEntity.ok(ApiType.success(true, message));
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

        // Map to AccountDetailsRes which matches the structure of LoginRes without tokens
        Page<AccountDetailsRes> getAccountsRes = getAccounts.map(account -> {
            var user = account.getUser();
            return AccountDetailsRes.builder()
                    .AccountID(account.getAccountID())
                    .UserName(account.getUserName())
                    .Email(account.getEmail())
                    .Phone(account.getPhone())
                    .Role(account.getRole())
                    .Active(account.getActive())
                    .Banned(account.getBanned())
                    .FirstName(user != null ? user.getFirstName() : null)
                    .LastName(user != null ? user.getLastName() : null)
                    .AvatarUrl(user != null ? user.getAvatar() : null)
                    .build();
        });

        // 3. Return data with ApiType format
        return ResponseEntity.ok(ApiType.success(getAccountsRes, "Get all accounts successfully"));
    }

    public AType addAccount(AddAccountReq addAccountReq) {

    if (accountRepository.existsByUserName(addAccountReq.getUserName())) {
        throw new CustomException(ErrorType.badRequest("Username already exists !"));
    }

    String passwordHash = passwordEncoder.encode(addAccountReq.getPassword());

    Account account = Account.builder()
            .userName(addAccountReq.getUserName())
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
            .role(savedAccount.getRole())
            .active(savedAccount.getActive())
            .banned(savedAccount.getBanned())
            .build();

    return ApiType.success(response, "Add account successfully");
    }

    public AType addMultiAccounts(AddAccountsReq request) {

    List<AccountRes> successAccounts = new ArrayList<>();
    List<AddAccountErrorRes> errors = new ArrayList<>();

    List<AddAccountReq> accounts = request.getAccounts();

    for (int i = 0; i < accounts.size(); i++) {
        AddAccountReq item = accounts.get(i);

        try {
         
            if (accountRepository.existsByUserName(item.getUserName())) {
                errors.add(AddAccountErrorRes.builder()
                        .index(i)
                        .userName(item.getUserName())
                        .reason("Username already exists")
                        .build());
                continue;
            }

            String passwordHash = passwordEncoder.encode(item.getPassword());

            Account account = Account.builder()
                    .userName(item.getUserName())
                    .passwordHash(passwordHash)
                    .loginType(LoginType.NORMAL)
                    .role(item.getRole() != null ? item.getRole() : Role.CUSTOMER)
                    .active(true)
                    .banned(false)
                    .isDeleted(false)
                    .build();

            Account savedAccount = accountRepository.save(account);

            AccountRes response = AccountRes.builder()
                    .accountID(savedAccount.getAccountID())
                    .userName(savedAccount.getUserName())
                    .email(savedAccount.getEmail())
                    .phone(savedAccount.getPhone())
                    .role(savedAccount.getRole())
                    .active(savedAccount.getActive())
                    .banned(savedAccount.getBanned())
                    .build();

            successAccounts.add(response);

        } catch (Exception e) {
            errors.add(AddAccountErrorRes.builder()
                    .index(i)
                    .userName(item.getUserName())
                    .reason(e.getMessage())
                    .build());
        }
    }

    AddAccountsRes response = AddAccountsRes.builder()
            .total(accounts.size())
            .successCount(successAccounts.size())
            .failedCount(errors.size())
            .successAccounts(successAccounts)
            .errors(errors)
            .build();

    return ApiType.success(response, "Add accounts completed");
}

}