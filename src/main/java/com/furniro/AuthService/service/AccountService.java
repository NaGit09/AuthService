package com.furniro.AuthService.service;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.entity.ExistingTokens;
import com.furniro.AuthService.database.entity.User;
import com.furniro.AuthService.database.repository.AccountRepository;
import com.furniro.AuthService.database.repository.TokenRepository;
import com.furniro.AuthService.database.repository.UserRepository;
import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ApiType;
import com.furniro.AuthService.dto.req.ChangePasswordReq;
import com.furniro.AuthService.dto.req.ConfirmOTPReq;
import com.furniro.AuthService.dto.req.LoginReq;
import com.furniro.AuthService.dto.req.RegisterReq;
import com.furniro.AuthService.dto.res.LoginRes;
import com.furniro.AuthService.exception.AuthException;
import com.furniro.AuthService.util.enums.AuthErrorCode;
import com.furniro.AuthService.util.UserUtils;
import com.furniro.AuthService.service.kafka.KafkaProducer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

@Service
@Slf4j
@RequiredArgsConstructor
@Validated
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final TokenRepository tokenRepository;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final KafkaProducer kafkaProducer;

    public ResponseEntity<AType> checkEmailExisted(@NonNull String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new AuthException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }
        return ResponseEntity.ok(ApiType.<Boolean>builder()
                .code(200)
                .message("Email is available")
                .data(true)
                .build());
    }

    public ResponseEntity<AType> registerAccount(@NonNull RegisterReq registerReq) {

        // 1. Check email not existed
        if (accountRepository.existsByEmail(registerReq.getEmail())) {
            throw new AuthException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 2. Create new account
        // 2.1 encode password
        String encodedPassword = passwordEncoder.encode(registerReq.getPassword());
        Account account = Account.builder()
                .userName(UserUtils.generateUniqueUsername())
                .email(registerReq.getEmail())
                .phone(registerReq.getNumberPhone())
                .passwordHash(encodedPassword)
                .build();

        account = accountRepository.save(account);

        // 3. Send message to MessageService with kafka topic : GenEmail
        Map<String, Object> message = new HashMap<>();
        message.put("firstName", registerReq.getFirstName());
        message.put("lastName", registerReq.getLastName());
        message.put("email", registerReq.getEmail());
        message.put("accountId", account.getAccountID());

        kafkaProducer.send("email.auth.active", message);
        // 4. Response for client
        AType result = ApiType.builder()
                .code(200)
                .message("Registration successful. Please check your email to activate account.")
                .data(true)
                .build();

        return ResponseEntity.ok().body(result);
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

        // 4. Sign access token
        String accessToken = jwtService.generateToken(account, "ACCESS");

        // 5. Find old refresh token if it has in DB
        ExistingTokens existingToken = tokenRepository.findByAccount(account)
                .orElse(new ExistingTokens());

        String refreshToken;

        // 6. if refresh token not exist in DB , create new existing token and save to
        // DB
        if (existingToken.getToken() == null ||
                jwtService.validateToken(existingToken.getToken(), "REFRESH")) {

            refreshToken = jwtService.generateToken(account, "REFRESH");
            existingToken.setAccount(account);
            existingToken.setToken(refreshToken);
            existingToken.setTokenType("REFRESH");
            tokenRepository.save(existingToken);

        } else {
            refreshToken = existingToken.getToken();
        }

        // 7. Get user info in DB
        User user = userRepository.findByAccount(account)
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND));

        // 8. Return data for client
        LoginRes res = LoginRes.builder()
                .AccessToken(accessToken)
                .RefreshToken(refreshToken)
                .FirstName(user.getFirstName())
                .LastName(user.getLastName())
                .UserName(account.getUserName())
                .AvatarUrl(user.getAvatar())
                .Email(account.getEmail())
                .Role(account.getRole())
                .build();
        // 9. Warn user login
        Map<String, Object> message = new HashMap<>();
        message.put("fullName", user.getFirstName() + " " + user.getLastName());
        message.put("email", account.getEmail());
        message.put("accountId", account.getAccountID());

        kafkaProducer.send("auth.warn.login", message);
        // 10. Return result
        return ResponseEntity.ok(ApiType.<LoginRes>builder()
                .code(200)
                .message("Login successful")
                .data(res)
                .build());
    }

    public ResponseEntity<AType> logoutAccount(@NonNull String token) {

        // 1. check token is refresh token and token don't expired
        boolean isValid = jwtService.validateToken(token, "REFRESH");
        log.info("authentication status : {}", isValid);

        if (!isValid) {
            throw new AuthException(AuthErrorCode.VERIFY_FAILED);
        }

        // 2. Get Existing Token from DB
        ExistingTokens existingToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_TOKEN));

        // 3. Delete token , prevent user logout too much
        tokenRepository.delete(existingToken);

        // 4. Return result
        AType success = ApiType.builder()
                .code(200)
                .message("Logout successful")
                .data(true)
                .build();

        return ResponseEntity.ok().body(success);
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
        AType success = ApiType.builder()
                .code(200)
                .message("OTP sent successfully")
                .data(true)
                .build();

        return ResponseEntity.ok().body(success);
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
        AType success = ApiType.builder()
                .code(200)
                .message("OTP confirmed successfully")
                .data(true)
                .build();
        return ResponseEntity.ok().body(success);
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

        AType success = ApiType.builder()
                .code(200)
                .message("Password changed successfully")
                .data(true)
                .build();

        return ResponseEntity.ok(success);
    }

    public ResponseEntity<AType> refreshToken(@NotEmpty String token) {

        // 1. check token is refresh token and token don't expired
        boolean isValid = jwtService.validateToken(token, "REFRESH");

        if (isValid) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        String username = jwtService.extractUsername(token);

        // 2. Check user existed
        Account account = accountRepository.findByUserName(username).orElseThrow(
                () -> new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND));

        // 3. Sign access token and return result
        String accessToken = jwtService.generateToken(account, "ACCESS");

        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Token refreshed successfully")
                .data(accessToken)
                .build());

    }

    // ADMIN API

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
        AType success = ApiType.builder()
                .code(200)
                .message(successMessage + " for " + result + "/" + accountIDs.size() + " account")
                .data(true)
                .build();

        return ResponseEntity.ok(success);
    }

    public ResponseEntity<AType> resetPassword(@NotEmpty List<Integer> ids) {
        String hashPassword = passwordEncoder.encode("furniro2026");
        // send mail notify user password is changed
        return executeBulkUpdate(ids, "Reset password", () -> accountRepository.resetPasswords(ids, hashPassword));
    }

    public ResponseEntity<AType> banAccount(@NotEmpty List<Integer> ids) {
        return executeBulkUpdate(ids, "Ban account", () -> accountRepository.banAccounts(ids));
    }

    public ResponseEntity<AType> unbanAccount(@NotEmpty List<Integer> ids) {
        return executeBulkUpdate(ids, "Unban account", () -> accountRepository.unbanAccounts(ids));
    }

    public ResponseEntity<AType> deleteAccount(@NotEmpty List<Integer> ids) {
        return executeBulkUpdate(ids, "Delete account", () -> accountRepository.deleteAccounts(ids));
    }
}
