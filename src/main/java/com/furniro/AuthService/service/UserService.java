package com.furniro.AuthService.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.entity.User;
import com.furniro.AuthService.database.repository.AccountRepository;
import com.furniro.AuthService.database.repository.UserRepository;
import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ApiType;
import com.furniro.AuthService.dto.API.ErrorType;
import com.furniro.AuthService.dto.req.UserReq;
import com.furniro.AuthService.exception.CustomException;
import com.furniro.AuthService.mapper.UserMapper;
import com.furniro.AuthService.service.other.KafkaProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final UserMapper userMapper;
    private final KafkaProducer producer;

    public ResponseEntity<AType> getUserById(Integer id) {
        // 1. check user existed
        User user = userRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorType.notFound("User not found !")));

        return ResponseEntity.ok(ApiType.success(user, "Get user by " + id + " success"));
    }

    public ResponseEntity<AType> getAllUser() {
        return ResponseEntity.ok(ApiType.success(userRepository.findAll(), "Get all user success"));
    }

    public ResponseEntity<AType> deleteUserById(Integer id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiType.success(null, "Delete user by " + id + " success"));
    }

    public ResponseEntity<AType> updateUserById(UserReq req) {

        // 1. check user existed
        User user = userRepository.findById(req.getUserID()).orElseThrow(
                () -> new CustomException(ErrorType.notFound("User not found !")));

        Account account = accountRepository.findById(req.getUserID()).orElseThrow(
                () -> new CustomException(ErrorType.notFound("User not found !")));

        // 2. update user and account username
        userMapper.updateUserFromReq(req, user);

        account.setUserName(req.getUsername());

        

        // 3. emit kafka event : upload.active
        Map<String, Object> message = new HashMap<>();
        message.put("fileID", req.getAvatarID());
        producer.send("upload.active", message);
        // 3. save user
        userRepository.save(user);
        accountRepository.save(account);

        // 4. return response
        return ResponseEntity.ok(ApiType.success(user, "Update user by " + req.getUserID() + " success"));
    }
}
