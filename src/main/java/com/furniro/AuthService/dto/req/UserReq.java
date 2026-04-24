package com.furniro.AuthService.dto.req;

import java.time.LocalDate;

import com.furniro.AuthService.util.enums.Gender;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserReq {
    
    
    private Integer userID;
    @Size(max = 50, message = "First name maximum 50 characters")
    
    private String firstName;
    @Size(max = 50, message = "Last name maximum 50 characters")
    private String lastName;
    
    private String avatarID;

    private String avatar;
    
    private Gender gender;
    
    @Past(message = "Date of birth must be a past date")
    private LocalDate dateOfBirth;

    private Integer accountID;
}
