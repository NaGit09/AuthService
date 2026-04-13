package com.furniro.AuthService.database.entity;
import com.furniro.AuthService.util.LoginType;
import com.furniro.AuthService.util.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accountID;

    @Column(unique = true, nullable = false, length = 50 , name = "UserName")
    private String userName;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "PasswordHash")
    private String passwordHash;

    private String providerID;

    @Enumerated(EnumType.STRING)
    private LoginType loginType = LoginType.NORMAL;

    @Enumerated(EnumType.STRING)
    private Role role = Role.CUSTOMER;

    @Column(name = "Active")
    private Boolean active = false;

    @Column(name = "Banned")
    private Boolean banned = false;

    @Column(name = "IsDeleted")
    private Boolean isDeleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Quan hệ 1-1 với User
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private User user;
}