package com.furniro.AuthService.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.entity.User;
import com.furniro.AuthService.database.repository.AccountRepository;
import com.furniro.AuthService.database.repository.UserRepository;
import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ApiType;
import com.furniro.AuthService.dto.req.UserReq;
import com.furniro.AuthService.exception.UserException;
import com.furniro.AuthService.util.enums.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public ResponseEntity<AType> createUser(UserReq req) {
        // 1. check account existed
        Account account = accountRepository.findById(req.getAccountID()).orElseThrow(
                () -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 2. create user
        User newUser = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .avatarID(req.getAvatarID())
                .avatar(req.getAvatar())
                .gender(req.getGender())
                .dateOfBirth(req.getDateOfBirth())
                .account(account)
                .build();

        // 3. save user
        userRepository.save(newUser);

        // 4. sent message ImageActive to upload service via kafka
        

        // 4. return response
        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Create user success")
                .data(newUser)
                .build());
    }

    public ResponseEntity<AType> getUserById(Integer id) {
        // 1. check user existed
        User user = userRepository.findById(id).orElseThrow(
                () -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Get user by " + id + " success")
                .data(user)
                .build());
    }

    public ResponseEntity<AType> getAllUser() {
        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Get all user success")
                .data(userRepository.findAll())
                .build());
    }

    public ResponseEntity<AType> deleteUserById(Integer id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Delete user by " + id + " success")
                .build());
    }

    public ResponseEntity<AType> updateUserById(UserReq req) {
        
        // 1. check user existed
        User user = userRepository.findById(req.getUserID()).orElseThrow(
                () -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 2. update user
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setAvatar(req.getAvatar());
        user.setGender(req.getGender());
        user.setDateOfBirth(req.getDateOfBirth());

        // 3. save user
        userRepository.save(user);

        // 4. return response
        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Update user by " + req.getUserID() + " success")
                .data(user)
                .build());
    }
}
