package com.furniro.AuthService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.req.UserReq;
import com.furniro.AuthService.service.UserService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<AType> getUser(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AType> updateUser(@RequestBody UserReq user) {
        return userService.updateUserById(user);
    }

    @PostMapping
    public ResponseEntity<AType> createUser(@RequestBody UserReq user) {
        return userService.createUser(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AType> deleteUser(@PathVariable Integer id) {
        return userService.deleteUserById(id);
    }
}
