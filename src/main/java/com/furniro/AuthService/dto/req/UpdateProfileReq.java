package com.furniro.AuthService.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import com.furniro.AuthService.util.AddressType;
import com.furniro.AuthService.util.Gender;

import java.time.LocalDate;

@Data

public class UpdateProfileReq {
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")

    private String username;

    @Email(message = "Invalid email")

    private String email;

    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")

    private String password;

    @Pattern(regexp = "^0[3-9]\\d{8}$", message = "Invalid phone number")

    private String phone;

    @Size(max = 50, message = "Last name maximum 50 characters")

    private String firstName;

    @Size(max = 50, message = "First name maximum 50 characters")

    private String lastName;

    private Gender gender;

    private MultipartFile avatar;

    @Past(message = "Date of birth must be a past date")

    private LocalDate birthday;

    // Delivery Information

    @Size(max = 100, message = "Recipient name maximum 100 characters")

    private String receiveName;

    @Pattern(regexp = "^0[3-9]\\d{8}$", message = "Invalid delivery phone number")

    private String receivePhone;

    @Size(max = 50, message = "Province/City maximum 50 characters")

    private String province;

    @Size(max = 50, message = "District/County maximum 50 characters")

    private String district;

    @Size(max = 50, message = "Ward/Commune maximum 50 characters")

    private String ward;

    @Size(max = 255, message = "Address details maximum 255 characters")

    private String street;

    private AddressType type;
}
