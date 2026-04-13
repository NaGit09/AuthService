package com.furniro.AuthService.database.repository;

import com.furniro.AuthService.database.entity.Address;
import com.furniro.AuthService.database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    Optional<Address> findByUser(User user);
}