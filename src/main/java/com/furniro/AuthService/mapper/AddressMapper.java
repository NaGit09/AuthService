package com.furniro.AuthService.mapper;

import com.furniro.AuthService.database.entity.Address;
import com.furniro.AuthService.dto.req.AddressReq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddressMapper {
    @Mapping(target = "user", ignore = true)
    void updateAddressFromReq(AddressReq req, @MappingTarget Address address);
}
