package com.example.digital_wallet.user.mapper;



import com.example.digital_wallet.user.dto.request.RoleRequest;
import com.example.digital_wallet.user.dto.response.RoleResponse;
import com.example.digital_wallet.user.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissions" , ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
