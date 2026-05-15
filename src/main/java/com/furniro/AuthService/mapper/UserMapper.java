package com.furniro.AuthService.mapper;

import com.furniro.AuthService.database.entity.User;
import com.furniro.AuthService.dto.req.UserReq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    void updateUserFromReq(UserReq req, @MappingTarget User user);
}
