package com.example.digital_wallet.user.mapper;



import com.example.digital_wallet.user.dto.request.PermissionRequest;
import com.example.digital_wallet.user.dto.response.PermissionResponse;
import com.example.digital_wallet.user.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
