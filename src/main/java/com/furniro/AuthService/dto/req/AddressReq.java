package com.furniro.AuthService.dto.req;

import com.furniro.AuthService.util.enums.AddressType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressReq {

    private Integer addressID;

    private String receiverName;

    private String receiverPhone;

    private String province;

    private String district;

    private String ward;

    private String street;

    private Boolean isDefault;

    private AddressType addressType;

    private Integer userID;

}
