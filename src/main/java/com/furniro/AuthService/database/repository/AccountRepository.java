package com.furniro.AuthService.database.repository;

import com.furniro.AuthService.database.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByEmail(String email);

    Optional<Account> findByUserName(String username);

    boolean existsByEmail(String email);

    boolean existsByUserName(String username);


    long countByAccountIDIn(List<Integer> ids);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.passwordHash = :newPassword WHERE a.accountID IN :ids")
    int resetPasswords(@Param("ids") List<Integer> ids, @Param("newPassword") String newPassword);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.isDeleted = true WHERE a.accountID IN :ids")
    int deleteAccounts(@Param("ids") List<Integer> ids);

    @Modifying
    @Transactional // Bắt buộc phải có Transactional cho thao tác ghi
    @Query("UPDATE Account a SET a.banned = true WHERE a.accountID IN :ids")
    int banAccounts(@Param("ids") List<Integer> ids);

    @Modifying
    @Transactional // Bắt buộc phải có Transactional cho thao tác ghi
    @Query("UPDATE Account a SET a.banned = false WHERE a.accountID IN :ids")
    int unbanAccounts(@Param("ids") List<Integer> ids);

    @Modifying
    @Transactional
    int existsByAccountId(Integer accountID);
}
