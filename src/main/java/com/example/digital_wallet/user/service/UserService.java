package com.example.digital_wallet.user.service;


import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.user.dto.request.RegisterRequest;
import com.example.digital_wallet.user.dto.request.UserUpdateRequest;
import com.example.digital_wallet.user.dto.response.UserResponse;
import com.example.digital_wallet.user.dto.response.WalletResponse;
import com.example.digital_wallet.user.entity.Role;
import com.example.digital_wallet.user.entity.User;
import com.example.digital_wallet.user.entity.UserStatus;
import com.example.digital_wallet.user.mapper.UserMapper;
import com.example.digital_wallet.user.repository.RoleRepository;
import com.example.digital_wallet.user.repository.UserRepository;
import com.example.digital_wallet.wallet.entity.Wallet;
import com.example.digital_wallet.wallet.entity.WalletStatus;
import com.example.digital_wallet.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class UserService {

    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RoleRepository roleRepository;
    WalletRepository walletRepository;

    UserMapper userMapper;


    @Transactional
    public UserResponse register(RegisterRequest request)
    {
        if(userRepository.existsByUsername(request.username()))
            throw new AppException(ErrorCode.USER_EXISTED);


        User user = userMapper.toUser(request);

        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findById("CUSTOMER")
                        .orElseThrow(()-> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        roles.add(role);

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(roles);

        userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .status(WalletStatus.ACTIVE)
                .version(0L)
                .build();

        walletRepository.save(wallet);

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));
        List<User> users = userRepository.findAll();

        List<UserResponse> userResponses= users
                .stream()
                .map( user -> userMapper.toUserResponse(user))
                .toList();

        return userResponses;

    }


    public UserResponse getUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);

    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated())
            throw  new AppException(ErrorCode.UNAUTHENTICATED);
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));

        Optional<Wallet> wallet = walletRepository.findByUserId(user.getId());






        return wallet.isPresent()
                ? userMapper.toUserResponse(user, wallet.get())
                : userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(
                        ()->new AppException(ErrorCode.USER_NOT_EXISTED));
        List<Role> roles = Collections.emptyList();

        if (request.getRoles() != null) {
            roles = roleRepository.findAllById(request.getRoles());
        }


        userMapper.updateUser(user,request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(new HashSet<>(roles));
        return userMapper.toUserResponse(userRepository.save(user));
    }


    public String deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(
                        ()->new AppException(ErrorCode.USER_NOT_EXISTED));
        User tmp = user;
        userRepository.delete(user);

        return "User has been deleted";
    }

}
