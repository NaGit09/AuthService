package com.furniro.AuthService.database.entity;

import com.furniro.AuthService.util.Gender;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userID;

    @Column(length = 50, nullable = false)
    private String firstName;

    @Column(length = 50, nullable = false)
    private String lastName;

    @Column(length = 50, nullable = false)
    @Builder.Default
    private String avatarID = "DEFAULT_AVATAR";

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Builder.Default
    private String avatar = "https://res.cloudinary.com/dvi3xlou4/image/upload/v1748789340/user_rk7e65.png";

    private LocalDate dateOfBirth;

    @OneToOne
    @JoinColumn(name = "AccountID", referencedColumnName = "AccountID", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses;
}
