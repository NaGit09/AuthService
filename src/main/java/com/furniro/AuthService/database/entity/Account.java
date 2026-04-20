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
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountID")
    private Integer accountID;

    @Column(unique = true, nullable = false, length = 50, name = "UserName")
    private String userName;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "PasswordHash")
    private String passwordHash;

    private String providerID;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LoginType loginType = LoginType.NORMAL;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.CUSTOMER;

    @Column(name = "Active")
    @Builder.Default
    private Boolean active = false;

    @Column(name = "Banned")
    @Builder.Default
    private Boolean banned = false;

    @Column(name = "IsDeleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Quan hệ 1-1 với User
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private User user;
}