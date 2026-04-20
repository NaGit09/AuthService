package com.furniro.AuthService.service.kafka;

import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.entity.Address;
import com.furniro.AuthService.database.entity.User;
import com.furniro.AuthService.database.repository.AccountRepository;
import com.furniro.AuthService.database.repository.AddressRepository;
import com.furniro.AuthService.database.repository.UserRepository;
import com.furniro.AuthService.exception.AuthException;
import com.furniro.AuthService.util.enums.AuthErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
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

    @Transactional
    @KafkaListener(topics = "email.auth.active", groupId = "auth-service-group")
    public void listen(Map<String, Object> event) {


        // convert data from kafka message
        String firstName = (String) event.get("firstName");
        String lastName = (String) event.get("lastName");
        Integer accountId = (Integer) event.get("accountId");
        
        if (accountRepository.existsByAccountID(accountId)) {
            log.warn("User for account {} already exists. Skipping.", accountId);
            return;
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND));

        User newUser = User.builder()
                .account(account)
                .firstName(firstName)
                .lastName(lastName)
                .build();

        userRepository.save(newUser);

        Address address = Address.builder()
                .user(newUser)
                .build();
        addressRepository.save(address);

        log.info("Successfully processed profile for accountId: {}", event.get("accountId").toString());
    }

}