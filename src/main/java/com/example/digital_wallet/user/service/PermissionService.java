package com.example.digital_wallet.user.service;



import com.example.digital_wallet.user.dto.request.PermissionRequest;
import com.example.digital_wallet.user.dto.response.PermissionResponse;
import com.example.digital_wallet.user.entity.Permission;
import com.example.digital_wallet.user.mapper.PermissionMapper;
import com.example.digital_wallet.user.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class PermissionService {

    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse createPermission(PermissionRequest request)
    {
        Permission permission = permissionMapper.toPermission(request);

        permissionRepository.save(permission);


        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll()
    {
        List<Permission> permissions= permissionRepository.findAll();

        return permissions
                .stream()
                .map(permissionMapper::toPermissionResponse)
                .toList();
    }

    public void deletePermission(String name)
    {
        permissionRepository.deleteById(name);
    }

}
