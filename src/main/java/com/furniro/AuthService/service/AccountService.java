package com.furniro.AuthService.service;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.entity.User;
import com.furniro.AuthService.database.repository.AccountRepository;
import com.furniro.AuthService.database.repository.UserRepository;
import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ApiType;
import com.furniro.AuthService.dto.req.ChangePasswordReq;
import com.furniro.AuthService.dto.req.ConfirmOTPReq;
import com.furniro.AuthService.dto.req.LoginReq;
import com.furniro.AuthService.dto.req.RegisterReq;
import com.furniro.AuthService.dto.res.LoginRes;
import com.furniro.AuthService.exception.imp.AuthException;
import com.furniro.AuthService.mapper.AuthMapper;
import com.furniro.AuthService.util.UserUtils;
import com.furniro.AuthService.util.error.AuthErrorCode;
import com.furniro.AuthService.service.other.JWTService;
import com.furniro.AuthService.service.other.KafkaProducer;
import com.furniro.AuthService.service.other.RedisService;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@Validated
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AddressService addressService;
    private final KafkaProducer kafkaProducer;
    private final AuthMapper authMapper;

    public ResponseEntity<AType> checkEmailExisted(@NonNull String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new AuthException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }
        return ResponseEntity.ok(ApiType.success(true));
    }

    @Transactional
    public ResponseEntity<AType> registerAccount(@NonNull RegisterReq registerReq) {

        String encodedPassword = passwordEncoder.encode(registerReq.getPassword());

        Account account = saveAccountAndProfile(registerReq, encodedPassword);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        Map<String, Object> message = new HashMap<>();
                        message.put("firstName", registerReq.getFirstName());
                        message.put("lastName", registerReq.getLastName());
                        message.put("accountID", account.getAccountID());
                        message.put("email", registerReq.getEmail());
                        kafkaProducer.send("auth.send.active", message);
                    }
                });

        LoginRes loginRes = generateLoginResponse(account);

        return ResponseEntity.ok(ApiType.success(loginRes, "Registration successful."));
    }

    @Transactional
    public Account saveAccountAndProfile(RegisterReq registerReq, String encodedPassword) {

        if (accountRepository.existsByEmail(registerReq.getEmail())) {
            throw new AuthException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String username = UserUtils.generateUniqueUsername();

        Account account = Account.builder()
                .userName(username)
                .email(registerReq.getEmail())
                .phone(registerReq.getNumberPhone())
                .passwordHash(encodedPassword)
                .active(true)
                .build();

        account = accountRepository.save(account);

        User user = userService.createUser(account,
                registerReq.getFirstName(),
                registerReq.getLastName());

        addressService.createAddress(user);

        return account;
    }

    public ResponseEntity<AType> activeAccount(@NonNull Integer accountID) {

        Account account = accountRepository.findById(accountID)
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND));

        if (account.getActive()) {
            return ResponseEntity.ok(ApiType.success(false, "Account is already activated"));
        }

        account.setActive(true);

        accountRepository.save(account);

        return ResponseEntity.ok(ApiType.success(true, "Account activated successfully"));
    }

    public ResponseEntity<AType> loginAccount(@NonNull LoginReq loginReq) {

        // 1. Check account existed
        Account account = accountRepository.findByEmail(loginReq.getEmail())
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND));

        // 2. Check user was active or baned account
        if (Boolean.FALSE.equals(account.getActive())) {
            throw new AuthException(AuthErrorCode.ACCOUNT_NOT_ACTIVE);
        }

        if (Boolean.TRUE.equals(account.getBanned())) {
            throw new AuthException(AuthErrorCode.ACCOUNT_IS_BANED);
        }

        // 3. Check password is match
        if (!passwordEncoder.matches(loginReq.getPassword(), account.getPasswordHash())) {
            throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
        }

        // 4. Generate login response
        LoginRes res = generateLoginResponse(account);

        // 5. Return result
        return ResponseEntity.ok(ApiType.success(res, "Login successful"));
    }

    private LoginRes generateLoginResponse(Account account) {
        // 1. Sign access token
        String accessToken = jwtService.generateToken(account, "ACCESS");

        // 2. Sign refresh token
        String refreshToken = jwtService.generateToken(account, "REFRESH");

        // 3. Get user info in DB
        User user = userRepository.findByAccount(account)
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND));

        // 5. Return data for client
        return authMapper.toLoginRes(account, user, accessToken, refreshToken);
    }

    public ResponseEntity<AType> logoutAccount(@NonNull String token) {

        // 1. check token is refresh token and token don't expired
        boolean isValid = jwtService.validateToken(token, "REFRESH");
        log.info("authentication status : {}", isValid);

        if (!isValid) {
            throw new AuthException(AuthErrorCode.VERIFY_FAILED);
        }

        // 2. Return result
        return ResponseEntity.ok(ApiType.success(true, "Logout successful"));
    }

    public ResponseEntity<AType> sendOTP(@NonNull String email) {

        // 1. Check has OTP key in redis
        String cachingKey = "OTP:" + email;

        boolean hasKey = redisService.isCaching(cachingKey);

        if (hasKey) {
            throw new AuthException(AuthErrorCode.OTP_EXPIRED);
        }

        // 2. Check user exists
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND));

        // 3. Check user is active
        if (Boolean.FALSE.equals(account.getActive())) {
            throw new AuthException(AuthErrorCode.ACCOUNT_NOT_ACTIVE);
        }

        // 4. Create random OTP
        String otp = String.valueOf(new Random().nextInt(899999) + 100000);

        // 5. Send OTP via MAIL public kafka topic : GenOTPForgot
        // mailService.sendMailOTP(account.getUserName(), email, otp);
        Map<String, Object> message = new HashMap<>();
        message.put("userName", account.getUserName());
        message.put("email", email);
        message.put("otp", otp);

        kafkaProducer.send("auth.send.otp", message);

        // 5. Save OTP to Redis with TTL is 5 minutes
        redisService.addData(cachingKey, otp, 5, TimeUnit.MINUTES);

        // 6. Return result
        return ResponseEntity.ok(ApiType.success(true, "OTP sent successfully"));
    }

    public ResponseEntity<AType> confirmOTP(@NonNull ConfirmOTPReq confirmOTPReq) {

        // 1. Get OTP from Redis
        String optKey = "OTP:" + confirmOTPReq.getEmail();

        String otpExist = redisService.getData(optKey);

        // 2. Check OTP existed
        if (otpExist == null) {
            throw new AuthException(AuthErrorCode.NOT_FOUND_OTP);
        }

        // 3. Check OTP matched
        if (!otpExist.equals(confirmOTPReq.getOtp())) {
            throw new AuthException(AuthErrorCode.OTP_NOT_MATCH);
        }

        // 4. return result for user
        redisService.removeData(optKey);
        return ResponseEntity.ok(ApiType.success(true, "OTP confirmed successfully"));
    }

    public ResponseEntity<AType> changePassword(ChangePasswordReq req) {
        // 1. Check OTP is existed Redis
        String cachingKey = "OTP:" + req.getEmail();

        boolean hasKey = redisService.isCaching(cachingKey);
        if (hasKey) {
            throw new AuthException(AuthErrorCode.OTP_EXPIRED);
        }

        // 2.Check user existed
        Account account = accountRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND));

        // 3. Compare password
        String oldPassword = req.getPassword();
        String newPassword = req.getConfirmPassword();

        if (!oldPassword.equals(newPassword)) {
            throw new AuthException(AuthErrorCode.PASSWORD_NOT_MATCH);
        }

        // 4. Save new password and return result
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        return ResponseEntity.ok(ApiType.success(true, "Password changed successfully"));
    }

    public ResponseEntity<AType> refreshToken(@NotEmpty String token) {

        // 1. check token is refresh token and token don't expired
        boolean isValid = jwtService.validateToken(token, "REFRESH");

        if (!isValid) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        String username = jwtService.extractUsername(token);

        // 2. Check user existed
        Account account = accountRepository.findByUserName(username).orElseThrow(
                () -> new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND));

        // 3. Sign access token and return result
        String accessToken = jwtService.generateToken(account, "ACCESS");

        return ResponseEntity.ok(ApiType.success(accessToken, "Token refreshed successfully"));
    }

}
