package com.furniro.AuthService.service.kafka;

import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.entity.Address;
import com.furniro.AuthService.database.entity.User;
import com.furniro.AuthService.database.repository.AccountRepository;
import com.furniro.AuthService.database.repository.AddressRepository;
import com.furniro.AuthService.database.repository.UserRepository;
import com.furniro.AuthService.exception.imp.AuthException;
import com.furniro.AuthService.util.error.AuthErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {
    private final UserRepository userRepo;
    private final AccountRepository accountRepo;
    private final AddressRepository addressRepo;

    @Transactional
    @KafkaListener(topics = "email.auth.active", groupId = "auth-service-group")
    public void listen(Map<String, Object> event) {

        if (accountRepo.existsByAccountID(Integer.valueOf(event.get("accountId").toString()))) {
            log.warn("User for account {} already exists. Skipping.", event.get("accountId").toString());
            return;
        }
        Account account = accountRepo.findById(Integer.valueOf(event.get("accountId").toString())).orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND));

        User newUser = User.builder().account(account).firstName((String) event.get("firstName")).lastName((String) event.get("lastName")).build();

        userRepo.save(newUser);

        Address address = Address.builder().user(newUser).build();
        addressRepo.save(address);
        log.info("Successfully processed profile for accountId: {}", event.get("accountId").toString());


    }


}