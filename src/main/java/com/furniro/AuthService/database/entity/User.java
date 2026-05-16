package com.furniro.AuthService.database.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.furniro.AuthService.util.enums.Gender;

@Entity
@Table(name = "users")
@Getter
@Setter
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

    @OneToOne(mappedBy = "user")
    @JsonBackReference
    private Account account;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Address> addresses;
}
