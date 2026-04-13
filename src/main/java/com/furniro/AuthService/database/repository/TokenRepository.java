package com.furniro.AuthService.database.repository;

import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.entity.ExistingTokens;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository  extends JpaRepository<ExistingTokens, Long> {

    // Find token with token receive
    Optional<ExistingTokens> findByToken(String token);

    // Find token with Account
    Optional<ExistingTokens> findByAccount(Account account);
}
