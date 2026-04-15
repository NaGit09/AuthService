package com.furniro.AuthService.service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.entity.Address;
import com.furniro.AuthService.database.entity.User;
import com.furniro.AuthService.database.repository.AccountRepository;
import com.furniro.AuthService.database.repository.AddressRepository;
import com.furniro.AuthService.database.repository.UserRepository;
import com.furniro.AuthService.exception.AuthException;
import com.furniro.AuthService.util.enums.AuthErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AddressRepository addressRepository;

    @Transactional // Ensures atomicity
    @KafkaListener(topics = "email.auth.active", groupId = "auth-service-group")
    public void listen(Map<String,Object> event) {

        // 1. Idempotency check
        if (accountRepository.existsByAccountId(Integer.valueOf(event.get("accountId").toString())) > 0) {
            log.warn("User for account {} already exists. Skipping.", event.get("accountId").toString());
            return;
        }

        // 2. Fetch Account
        Account account = accountRepository.findById(Integer.valueOf(event.get("accountId").toString()))
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND));

        // 3. Build & Persist
        User newUser = User.builder()
                .account(account)
                .firstName((String) event.get("firstName"))
                .lastName((String) event.get("lastName"))
                .build();

        userRepository.save(newUser);

        Address address = Address.builder()
                .user(newUser)
                .build();
        addressRepository.save(address);

        log.info("Successfully processed profile for accountId: {}", event.get("accountId").toString());
    }
}