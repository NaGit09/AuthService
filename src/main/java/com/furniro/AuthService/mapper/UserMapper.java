package com.furniro.AuthService.mapper;

import com.furniro.AuthService.database.entity.User;
import com.furniro.AuthService.dto.req.UserReq;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    void updateUserFromReq(UserReq req, @MappingTarget User user);
}
