package com.example.digital_wallet.user.controller;



import com.example.digital_wallet.common.response.ApiResponse;
import com.example.digital_wallet.user.dto.request.RoleRequest;
import com.example.digital_wallet.user.dto.response.RoleResponse;
import com.example.digital_wallet.user.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
@Slf4j
public class RoleController {
   RoleService roleService;

    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request)
    {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.createRole(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAll()
    {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @DeleteMapping("/{roleName}")
    ApiResponse delete(@PathVariable String roleName)
    {
        roleService.delete(roleName);
        return ApiResponse.builder()
                .message("Deleted")
                .build();
    }

}
