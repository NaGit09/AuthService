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
import com.furniro.AuthService.database.entity.Address;
import com.furniro.AuthService.database.entity.User;
import com.furniro.AuthService.database.repository.AccountRepository;
import com.furniro.AuthService.database.repository.AddressRepository;
import com.furniro.AuthService.database.repository.UserRepository;
import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ApiType;
import com.furniro.AuthService.dto.API.ErrorType;
import com.furniro.AuthService.dto.req.ChangePasswordReq;
import com.furniro.AuthService.dto.req.ConfirmOTPReq;
import com.furniro.AuthService.dto.req.LoginByUsernameReq;
import com.furniro.AuthService.dto.req.LoginReq;
import com.furniro.AuthService.dto.req.RegisterReq;
import com.furniro.AuthService.dto.res.LoginRes;
import com.furniro.AuthService.exception.CustomException;
import com.furniro.AuthService.mapper.AuthMapper;
import com.furniro.AuthService.util.UserUtils;
import com.furniro.AuthService.service.other.JWTService;
import com.furniro.AuthService.service.other.KafkaProducer;
import com.furniro.AuthService.service.other.RedisService;

import java.util.Date;
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
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    private final JWTService jwtService;
    private final RedisService redisService;

    private final KafkaProducer kafkaProducer;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;

    @Transactional
    public Account saveAccountAndProfile(RegisterReq registerReq, String encodedPassword) {

        if (accountRepository.existsByEmail(registerReq.getEmail())) {
            throw new CustomException(ErrorType
                    .badRequest("Email already exists"));
        }

        String username = UserUtils.generateUniqueUsername();

        User user = User.builder()
                .firstName(registerReq.getFirstName())
                .lastName(registerReq.getLastName())
                .build();

        userRepository.save(user);

        Account account = Account.builder()
                .userName(username)
                .email(registerReq.getEmail())
                .phone(registerReq.getNumberPhone())
                .passwordHash(encodedPassword)
                .active(true)
                .user(user)
                .build();

        account = accountRepository.save(account);

        Address address = new Address();
        address.setUser(user);
        addressRepository.save(address);

        return account;
    }

    private void validateLoginAccount(Account account, String rawPassword) {
        if (Boolean.FALSE.equals(account.getActive())) {
            throw new CustomException(ErrorType.badRequest("Account is not active"));
        }

        if (Boolean.TRUE.equals(account.getBanned())) {
            throw new CustomException(ErrorType.badRequest("Account is banned"));
        }

        if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            throw new CustomException(ErrorType.badRequest("Invalid password"));
        }

    }

    private LoginRes generateLoginResponse(Account account) {
        // 1. Sign access token
        String accessToken = jwtService.generateToken(account, "ACCESS");

        // 2. Sign refresh token
        String refreshToken = jwtService.generateToken(account, "REFRESH");

        // 3. Get user info in DB
        User user = userRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorType.notFound("Account not found")));

        // 5. Return data for client
        return authMapper.toLoginRes(account, user, accessToken, refreshToken);
    }

    public ResponseEntity<AType> checkEmailExisted(@NonNull String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new CustomException(ErrorType
                    .badRequest("Email already exists"));
        }
        return ResponseEntity.ok(ApiType.success(true));
    }

    @Transactional
    public ResponseEntity<AType> registerAccount(@NonNull RegisterReq registerReq) {

        String encodedPassword = passwordEncoder.encode(registerReq.getPassword());

        Account account = saveAccountAndProfile(registerReq, encodedPassword);

        Map<String, Object> message = new HashMap<>();
        message.put("firstName", registerReq.getFirstName());
        message.put("lastName", registerReq.getLastName());
        message.put("accountID", account.getAccountID());
        message.put("email", registerReq.getEmail());

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        kafkaProducer.send("auth.send.active", message);
                    }
                });

        LoginRes loginRes = generateLoginResponse(account);

        return ResponseEntity.ok(ApiType
                .success(loginRes, "Registration successful."));
    }

    public ResponseEntity<AType> activeAccount(@NonNull Integer accountID) {

        Account account = accountRepository.findById(accountID)
                .orElseThrow(() -> new CustomException(ErrorType
                        .notFound("Account not found")));

        if (account.getActive()) {
            return ResponseEntity.ok(ApiType
                    .success(false, "Account is already activated"));
        }

        account.setActive(true);

        accountRepository.save(account);

        return ResponseEntity.ok(ApiType
                .success(true, "Account activated successfully"));
    }

    public ResponseEntity<AType> loginByEmail(@NonNull LoginReq loginReq) {
        Account account = accountRepository.findByEmail(loginReq.getEmail())
                .orElseThrow(() -> new CustomException(ErrorType.notFound("Account not found")));

        validateLoginAccount(account, loginReq.getPassword());

        LoginRes res = generateLoginResponse(account);

        return ResponseEntity.ok(ApiType.success(res, "Login successful"));
    }

    public ResponseEntity<AType> loginByUsername(@NonNull LoginByUsernameReq req) {
        Account account = accountRepository.findByUserName(req.getUserName())
                .orElseThrow(() -> new CustomException(ErrorType.notFound("Account not found")));

        validateLoginAccount(account, req.getPassword());

        LoginRes res = generateLoginResponse(account);

        return ResponseEntity.ok(ApiType.success(res, "Login successful"));
    }

    public ResponseEntity<AType> sendOTP(@NonNull String email) {

        // 1. Check has OTP key in redis
        String cachingKey = "OTP:" + email;

        boolean hasKey = redisService.isCaching(cachingKey);

        if (hasKey) {
            throw new CustomException(ErrorType
                    .badRequest("OTP has already sent, please wait for a while"));
        }

        // 2. Check user exists
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorType
                        .notFound("Account not found")));

        // 3. Check user is active
        if (Boolean.FALSE.equals(account.getActive())) {
            throw new CustomException(ErrorType.badRequest("Account is not active"));
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
        String otpKey = "OTP:" + confirmOTPReq.getEmail();
        String otpExist = redisService.getData(otpKey);

        if (otpExist == null) {
            throw new CustomException(ErrorType.badRequest("OTP has expired or does not exist"));
        }

        if (!otpExist.equals(confirmOTPReq.getOtp())) {
            throw new CustomException(ErrorType.badRequest("Invalid OTP code"));
        }

        // 1. Invalidate the OTP key
        redisService.removeData(otpKey);

        // 2. Generate a secure, short-lived reset token (valid for 5 minutes)
        String resetToken = java.util.UUID.randomUUID().toString();
        String verificationKey = "OTP_VERIFIED:" + confirmOTPReq.getEmail();
        redisService.addData(verificationKey, resetToken, 5, TimeUnit.MINUTES);

        // 3. Return the reset token to the client
        Map<String, String> responseData = Map.of(
                "email", confirmOTPReq.getEmail(),
                "resetToken", resetToken);
        return ResponseEntity.ok(ApiType.success(responseData, "OTP confirmed successfully"));
    }

    public ResponseEntity<AType> changePassword(ChangePasswordReq req) {
        // 1. Verify the Reset Token exists in Redis
        String verificationKey = "OTP_VERIFIED:" + req.getEmail();
        String cachedToken = redisService.getData(verificationKey);

        if (cachedToken == null || !cachedToken.equals(req.getResetToken())) {
            throw new CustomException(ErrorType.unauthorized("Unauthorized password reset attempt. Verify OTP first."));
        }

        // 2. Check user exists
        Account account = accountRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new CustomException(ErrorType.notFound("Account not found")));

        // 3. Validate new passwords match
        String newPassword = req.getPassword();
        String confirmPassword = req.getConfirmPassword();

        if (!newPassword.equals(confirmPassword)) {
            throw new CustomException(ErrorType.badRequest("Passwords do not match"));
        }

        // 4. Save new password and revoke the reset token immediately (Prevention of
        // Replay Attacks)
        account.setPasswordHash(passwordEncoder.encode(newPassword));

        accountRepository.save(account);
        redisService.removeData(verificationKey);

        return ResponseEntity.ok(ApiType.success(true, "Password changed successfully"));
    }

    public ResponseEntity<AType> refreshToken(@NotEmpty String token) {

        log.info("refreshToken: {}", token);
        // 1. check token is refresh token and token don't expired
        boolean isValid = jwtService.validateToken(token, "REFRESH");

        if (!isValid) {
            throw new CustomException(ErrorType
                    .badRequest("Invalid token"));
        }
        // 1.1 check refresh token exist in blacklist
        String tokenBlack = "BLACKLISTED_TOKEN:" + jwtService.extractTokenId(token);

        if (redisService.getData(tokenBlack) != null) {
            throw new CustomException(ErrorType
                    .badRequest("Token has been blacklisted, please login again"));
        }

        String username = jwtService.extractUsername(token);

        // 2. Check user existed
        Account account = accountRepository.findByUserName(username).orElseThrow(
                () -> new CustomException(ErrorType.notFound("Account not found")));

        // 3. Sign access token and return result
        String accessToken = jwtService.generateToken(account, "ACCESS");

        return ResponseEntity.ok(ApiType.success(accessToken));
    }

    public ResponseEntity<AType> logoutAccount(@NonNull String accessToken, @NonNull String refreshToken) {

        // 1. Validate and blacklist the Refresh Token
        boolean isRefreshValid = jwtService.validateToken(refreshToken, "REFRESH");
        if (!isRefreshValid) {
            throw new CustomException(ErrorType.badRequest("Invalid or already expired refresh token"));
        }

        String refreshId = jwtService.extractTokenId(refreshToken);
        Date refreshExp = jwtService.extractExpiration(refreshToken);
        long refreshRemaining = refreshExp.getTime() - System.currentTimeMillis();

        if (refreshRemaining > 0) {
            String blacklistKey = "BLACKLISTED_TOKEN:" + refreshId;
            redisService.addData(blacklistKey, "true", refreshRemaining, TimeUnit.MILLISECONDS);
        }

        // 2. Validate and blacklist the Access Token
        boolean isAccessValid = jwtService.validateToken(accessToken, "ACCESS");
        if (isAccessValid) {
            String accessId = jwtService.extractTokenId(accessToken);
            Date accessExp = jwtService.extractExpiration(accessToken);
            long accessRemaining = accessExp.getTime() - System.currentTimeMillis();

            if (accessRemaining > 0) {
                String blacklistKey = "BLACKLISTED_TOKEN:" + accessId;
                redisService.addData(blacklistKey, "true", accessRemaining, TimeUnit.MILLISECONDS);
            }
        }

        return ResponseEntity.ok(ApiType.success(true, "Logout successful"));
    }
}
