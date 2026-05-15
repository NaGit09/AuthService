package com.furniro.AuthService.database.entity;

import com.furniro.AuthService.util.enums.AddressType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Address")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer addressID;

    private String receiverName;

    private String receiverPhone;

    private String province;

    private String district;

    private String ward;

    private String street;

    @Builder.Default
    private Boolean isDefault = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AddressType addressType = AddressType.HOME;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;
}