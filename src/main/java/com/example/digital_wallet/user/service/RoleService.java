package com.example.digital_wallet.user.service;



import com.example.digital_wallet.user.dto.request.RoleRequest;
import com.example.digital_wallet.user.dto.response.RoleResponse;
import com.example.digital_wallet.user.entity.Role;
import com.example.digital_wallet.user.mapper.RoleMapper;
import com.example.digital_wallet.user.repository.PermissionRepository;
import com.example.digital_wallet.user.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class RoleService {
    private final PermissionRepository permissionRepository;
    RoleRepository roleRepository;

    RoleMapper roleMapper;

    public RoleResponse createRole(RoleRequest request)
    {
        Role role = roleMapper.toRole(request);

        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        roleRepository.save(role);

        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAll()
    {
        var roles = roleRepository.findAll();

        return roles.stream().map(roleMapper::toRoleResponse).toList();
    }

    public void delete(String name)
    {
        roleRepository.deleteById(name);
    }
}
