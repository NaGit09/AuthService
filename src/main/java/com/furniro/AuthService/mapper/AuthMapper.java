package com.furniro.AuthService.mapper;

import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.database.entity.User;
import com.furniro.AuthService.dto.res.LoginRes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "AccessToken", source = "accessToken")
    @Mapping(target = "RefreshToken", source = "refreshToken")
    @Mapping(target = "FirstName", source = "user.firstName")
    @Mapping(target = "LastName", source = "user.lastName")
    @Mapping(target = "UserName", source = "account.userName")
    @Mapping(target = "AvatarUrl", source = "user.avatar")
    @Mapping(target = "Email", source = "account.email")
    @Mapping(target = "Role", source = "account.role")
    LoginRes toLoginRes(Account account, User user, String accessToken, String refreshToken);
}
