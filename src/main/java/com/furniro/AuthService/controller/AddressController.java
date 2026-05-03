package com.furniro.AuthService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.req.AddressReq;
import com.furniro.AuthService.service.AddressService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @GetMapping("/{id}")
    public ResponseEntity<AType> getAddress
    (@PathVariable Integer id) {
        return addressService.getAddress(id);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<AType> getAddressByUserId
    (@PathVariable Integer userId) {
        return addressService.getAddressByUser(userId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AType> updateAddress(
        @RequestBody AddressReq address) {
        return addressService.updateAddress(address);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AType> deleteAddress
        (@PathVariable Integer id) {
        return addressService.deleteAddress(id);
    }
}
