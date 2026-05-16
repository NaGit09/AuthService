package com.furniro.AuthService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> getAllUser() {
        return userService.getAllUser();
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<AType> updateUserById(@RequestBody UserReq req) {
        return userService.updateUserById(req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AType> deleteUserById(@PathVariable Integer id) {
        return userService.deleteUserById(id);
    }
}
