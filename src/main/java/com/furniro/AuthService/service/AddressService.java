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
import com.furniro.AuthService.dto.API.ErrorType;
import com.furniro.AuthService.dto.req.AddressReq;
import com.furniro.AuthService.exception.CustomException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public ResponseEntity<AType> updateAddress
    (@NonNull AddressReq updateAddressReq) {
        // 1. Check user exist
        User user = userRepository.findById(updateAddressReq.getUserID())
                .orElseThrow(() -> new CustomException(ErrorType
                        .notFound("User not found !")));

        // 2. Create or Update address
        Address address;
        if (updateAddressReq.getAddressID() == null) {
            address = new Address();
        } else {
            address = addressRepository.findById(updateAddressReq.getAddressID())
                    .orElseThrow(() -> new CustomException(ErrorType
                            .notFound("Address not found !")));
        }

        address.setAddressType(updateAddressReq.getAddressType());  

        address.setProvince(updateAddressReq.getProvince());

        address.setDistrict(updateAddressReq.getDistrict());

        address.setWard(updateAddressReq.getWard());

        address.setStreet(updateAddressReq.getStreet());

        address.setReceiverName(updateAddressReq.getReceiverName());

        address.setReceiverPhone(updateAddressReq.getReceiverPhone());

        address.setIsDefault(updateAddressReq.getIsDefault());

        // 3. Update address
        address.setUser(user);

        // 4. Save address
        addressRepository.save(address);

        // 5. Return response
        return ResponseEntity.ok(ApiType
                .success(address, "Address updated successfully"));
    }
    
    public ResponseEntity<AType> deleteAddress
    (@NonNull Integer addressID) {
        // 1. Check address exist
        Address address = addressRepository.findById(addressID)
                .orElseThrow(() -> new CustomException(ErrorType
                    .notFound("Address not found !")));
        
        // 2. Delete address
        addressRepository.delete(address);
        
        // 3. Return response
        return ResponseEntity.ok(ApiType
                .success(null, "Address deleted successfully"));
    }

    public ResponseEntity<AType> getAddress
    (@NonNull Integer addressID) {
        // 1. Check address exist
        Address address = addressRepository.findById(addressID)
                .orElseThrow(() -> new CustomException(ErrorType
                    .notFound("Address not found !")));
        
        // 2. Return response
        return ResponseEntity.ok(ApiType
                .success(address, "Address found successfully"));
    }

    public ResponseEntity<AType> getAddressByUser
    (@NonNull Integer userID) {
        // 1. Check user exist
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new CustomException(ErrorType
                    .notFound("User not found !")));
        
        // 2. Get address by user
        List<Address> addresses = addressRepository
                                    .findByUser(user)
                                    .stream()
                                    .toList();
        
        // 3. Return response
        return ResponseEntity.ok(ApiType
                .success(addresses, "Address found successfully"));
    }
}
