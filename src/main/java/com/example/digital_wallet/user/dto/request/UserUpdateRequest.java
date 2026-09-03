package com.example.digital_wallet.user.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserUpdateRequest {


    String password;

    String email;

    LocalDate dob;

    List<String> roles;
}
