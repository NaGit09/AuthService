package com.furniro.AuthService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.req.UserReq;
import com.furniro.AuthService.service.UserService;

import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<AType> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @GetMapping("/all")
    public ResponseEntity<AType> getAllUser() {
        return userService.getAllUser();
    }

    @PutMapping("/update")
    public ResponseEntity<AType> updateUserById(@RequestBody UserReq req) {
        return userService.updateUserById(req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AType> deleteUserById(@PathVariable Integer id) {
        return userService.deleteUserById(id);
    }
}
