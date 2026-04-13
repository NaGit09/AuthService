package com.furniro.AuthService.database.repository;
import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Tìm kiếm user kèm theo danh sách địa chỉ (dùng EntityGraph để tránh N+1)
    @EntityGraph(attributePaths = {"addresses"})
    Optional<User> findWithAddressesByUserID(Integer userID);

    Optional<User> findByAccount(Account account);
}
