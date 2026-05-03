package com.furniro.AuthService.service;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.entity.User;
import com.furniro.AuthService.database.repository.UserRepository;
import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ApiType;
import com.furniro.AuthService.dto.req.UserReq;
import com.furniro.AuthService.exception.imp.UserException;
import com.furniro.AuthService.util.error.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(Account account, String firstName, String lastName) {
        // 1. check account , first name , last name exist
        if (account == null || firstName == null || lastName == null) {
                throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }
        
        // 2. create user
        User newUser = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .account(account)
                .build();

        // 3. save user
        userRepository.save(newUser);

        return newUser;
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
