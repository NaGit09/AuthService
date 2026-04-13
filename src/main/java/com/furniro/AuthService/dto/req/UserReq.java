package com.furniro.AuthService.dto.req;

import java.time.LocalDate;

import com.furniro.AuthService.util.Gender;

import lombok.Data;

@Data
public class UserReq {
    
    
    private Integer userID;

    private String firstName;
    
    private String lastName;
    
    private String avatarID;

    private String avatar;
    
    private Gender gender;
    
    private LocalDate dateOfBirth;

    private Integer accountID;
}
