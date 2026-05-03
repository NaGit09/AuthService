package com.furniro.AuthService.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.furniro.AuthService.database.entity.Address;
import com.furniro.AuthService.database.entity.User;
import com.furniro.AuthService.database.repository.AddressRepository;
import com.furniro.AuthService.database.repository.UserRepository;
import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ApiType;
import com.furniro.AuthService.dto.req.AddressReq;
import com.furniro.AuthService.exception.imp.AddressException;
import com.furniro.AuthService.util.error.AddressErrorCode;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public Address createAddress(User user) {

        Address address = Address.builder()
                .user(user)
                .build();
        
        addressRepository.save(address);

        return address;
    }

    public ResponseEntity<AType> updateAddress
    (@NonNull AddressReq updateAddressReq) {
        // 1. Check user exist
        User user = userRepository.findById(updateAddressReq.getUserID())
                .orElseThrow(() -> new AddressException(AddressErrorCode.ADDRESS_NOT_FOUND));

        // 2. Check address exist
        Address address = addressRepository.findById(updateAddressReq.getAddressID())
                .orElseThrow(() -> new AddressException(AddressErrorCode.ADDRESS_NOT_FOUND));

        // 3. Update address
        address.setReceiverName(updateAddressReq.getReceiverName());
        address.setReceiverPhone(updateAddressReq.getReceiverPhone());
        address.setProvince(updateAddressReq.getProvince());
        address.setDistrict(updateAddressReq.getDistrict());
        address.setWard(updateAddressReq.getWard());
        address.setStreet(updateAddressReq.getStreet());
        address.setIsDefault(updateAddressReq.getIsDefault());
        address.setAddressType(updateAddressReq.getAddressType());
        address.setUser(user);

        // 4. Save address
        addressRepository.save(address);

        // 5. Return response
        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Address updated successfully")
                .data(address)
                .build());
    }
    
    public ResponseEntity<AType> deleteAddress
    (@NonNull Integer addressID) {
        // 1. Check address exist
        Address address = addressRepository.findById(addressID)
                .orElseThrow(() -> new AddressException(AddressErrorCode.ADDRESS_NOT_FOUND));
        
        // 2. Delete address
        addressRepository.delete(address);
        
        // 3. Return response
        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Address deleted successfully")
                .build());
    }

    public ResponseEntity<AType> getAddress
    (@NonNull Integer addressID) {
        // 1. Check address exist
        Address address = addressRepository.findById(addressID)
                .orElseThrow(() -> new AddressException(AddressErrorCode.ADDRESS_NOT_FOUND));
        
        // 2. Return response
        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Address found successfully")
                .data(address)
                .build());
    }

    public ResponseEntity<AType> getAddressByUser
    (@NonNull Integer userID) {
        // 1. Check user exist
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new AddressException(AddressErrorCode.ADDRESS_NOT_FOUND));
        
        // 2. Get address by user
        List<Address> addresses = addressRepository.findByUser(user).stream().toList();
        
        // 3. Return response
        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Address found successfully")
                .data(addresses)
                .build());
    }
}
