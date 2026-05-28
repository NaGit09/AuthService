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
import com.furniro.AuthService.dto.API.ErrorType;
import com.furniro.AuthService.dto.req.ChangePasswordReq;
import com.furniro.AuthService.dto.req.ConfirmOTPReq;
import com.furniro.AuthService.dto.req.LoginReq;
import com.furniro.AuthService.dto.req.RegisterReq;
import com.furniro.AuthService.dto.res.LoginRes;
import com.furniro.AuthService.exception.CustomException;
import com.furniro.AuthService.mapper.AuthMapper;
import com.furniro.AuthService.util.UserUtils;
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

    private final AddressService addressService;
    private final KafkaProducer kafkaProducer;
    private final AuthMapper authMapper;

    public ResponseEntity<AType> checkEmailExisted(@NonNull String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new CustomException(ErrorType.badRequest("Email already exists"));
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
            throw new CustomException(ErrorType.badRequest("Email already exists"));
        }

        String username = UserUtils.generateUniqueUsername();

        User user = User.builder()
                .firstName(registerReq.getFirstName())
                .lastName(registerReq.getLastName())
                .build();

        Account account = Account.builder()
                .userName(username)
                .email(registerReq.getEmail())
                .phone(registerReq.getNumberPhone())
                .passwordHash(encodedPassword)
                .active(true)
                .user(user)
                .build();

        account = accountRepository.save(account);

        addressService.createAddress(user);

        return account;
    }

    public ResponseEntity<AType> activeAccount(@NonNull Integer accountID) {

        Account account = accountRepository.findById(accountID)
                .orElseThrow(() -> new CustomException(ErrorType.notFound("Account not found")));

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
                .orElseThrow(() -> new CustomException(ErrorType.notFound("Account not found")));

        // 2. Check user was active or baned account
        if (Boolean.FALSE.equals(account.getActive())) {
            throw new CustomException(ErrorType.badRequest("Account is not active"));
        }

        if (Boolean.TRUE.equals(account.getBanned())) {
            throw new CustomException(ErrorType.badRequest("Account is banned"));
        }

        // 3. Check password is match
        if (!passwordEncoder.matches(loginReq.getPassword(), account.getPasswordHash())) {
            throw new CustomException(ErrorType.badRequest("Invalid password"));
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
                .orElseThrow(() -> new CustomException(ErrorType.notFound("Account not found")));

        // 5. Return data for client
        return authMapper.toLoginRes(account, user, accessToken, refreshToken);
    }

    public ResponseEntity<AType> logoutAccount(@NonNull String token) {

        // 1. check token is refresh token and token don't expired
        boolean isValid = jwtService.validateToken(token, "REFRESH");
        log.info("authentication status : {}", isValid);

        if (!isValid) {
            throw new CustomException(ErrorType.badRequest("Invalid token"));
        }

        // 2. Return result
        return ResponseEntity.ok(ApiType.success(true, "Logout successful"));
    }

    public ResponseEntity<AType> sendOTP(@NonNull String email) {

        // 1. Check has OTP key in redis
        String cachingKey = "OTP:" + email;

        boolean hasKey = redisService.isCaching(cachingKey);

        if (hasKey) {
            throw new CustomException(ErrorType.badRequest("OTP has already sent, please wait for a while"));
        }

        // 2. Check user exists
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorType.notFound("Account not found")));

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

        // 1. Get OTP from Redis
        String optKey = "OTP:" + confirmOTPReq.getEmail();

        String otpExist = redisService.getData(optKey);

        // 2. Check OTP existed
        if (otpExist == null) {
            throw new CustomException(ErrorType.notFound("OTP not found"));
        }

        // 3. Check OTP matched
        if (!otpExist.equals(confirmOTPReq.getOtp())) {
            throw new CustomException(ErrorType.badRequest("OTP not match"));
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
            throw new CustomException(ErrorType.badRequest("OTP has already sent, please wait for a while"));
        }

        // 2.Check user existed
        Account account = accountRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new CustomException(ErrorType.notFound("Account not found")));

        // 3. Compare password
        String oldPassword = req.getPassword();
        String newPassword = req.getConfirmPassword();

        if (!oldPassword.equals(newPassword)) {
            throw new CustomException(ErrorType.badRequest("Password not match"));
        }

        // 4. Save new password and return result
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        return ResponseEntity.ok(ApiType.success(true, "Password changed successfully"));
    }

    public ResponseEntity<AType> refreshToken(@NotEmpty String token) {

        log.info("refreshToken: {}", token);
        // 1. check token is refresh token and token don't expired
        boolean isValid = jwtService.validateToken(token, "REFRESH");

        if (!isValid) {
            throw new CustomException(ErrorType.badRequest("Invalid token"));
        }

        String username = jwtService.extractUsername(token);

        // 2. Check user existed
        Account account = accountRepository.findByUserName(username).orElseThrow(
                () -> new CustomException(ErrorType.notFound("Account not found")));

        // 3. Sign access token and return result
        String accessToken = jwtService.generateToken(account, "ACCESS");

        return ResponseEntity.ok(ApiType.success(accessToken, "Token refreshed successfully"));
    }

}
