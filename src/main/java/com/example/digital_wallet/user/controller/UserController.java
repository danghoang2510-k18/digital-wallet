package com.example.digital_wallet.user.controller;


import com.example.digital_wallet.common.response.ApiResponse;
import com.example.digital_wallet.user.dto.request.RegisterRequest;
import com.example.digital_wallet.user.dto.request.UserUpdateRequest;
import com.example.digital_wallet.user.dto.response.UserResponse;
import com.example.digital_wallet.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserController {

    UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid RegisterRequest request)
    {
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("User registered susscess")
                .result(userService.register(request))
                .build();
    }

    @GetMapping("/getUsers")
    ApiResponse<List<UserResponse>> getUsers()
    {

        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable UUID userId)
    {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();

    }

    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo()
    {

        return
                ApiResponse.<UserResponse>builder()
                        .result(userService.getMyInfo())
                        .build();

    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable UUID userId , @RequestBody @Valid UserUpdateRequest request)
    {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId,request))
                .build();


    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable UUID userId)
    {
        return
                ApiResponse.<String>builder()
                        .result(userService.deleteUser(userId))
                        .build();
    }
}
