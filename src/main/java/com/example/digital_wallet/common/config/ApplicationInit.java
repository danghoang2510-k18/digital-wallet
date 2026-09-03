package com.example.digital_wallet.common.config;

import com.example.digital_wallet.user.entity.User;
import com.example.digital_wallet.user.entity.UserStatus;
import com.example.digital_wallet.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class ApplicationInit {

    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository)
    {
        return args -> {
            if(userRepository.findByUsername("admin").isEmpty())
            {
                var roles = new HashSet<String>();
//                roles.add(Role.ADMIN.name());
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .email("admin@gmail.com")
                        .status(UserStatus.ACTIVE)
//                        .roles(roles)
                        .build();

                userRepository.save(user);

                log.warn("admin user has been created with default password: admin");
            }
        };
    }
}
