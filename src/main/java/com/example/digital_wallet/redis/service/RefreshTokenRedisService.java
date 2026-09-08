package com.example.digital_wallet.redis.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class RefreshTokenRedisService {

    static String PREFIX = "refresh-token:";

    StringRedisTemplate redisTemplate;

    public void save(
            String jti,
            UUID userId,
            Duration ttl
    ) {
        log.info("Cookies has saved");
        redisTemplate.opsForValue()
                .set(
                        PREFIX + jti,
                        userId.toString(),
                        ttl
                );
    }

    public boolean exists(String jti) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(PREFIX + jti)
        );
    }

    public void revoke(String jti) {
        log.info("Cookies has deleted");
        redisTemplate.delete(PREFIX + jti);
    }
}
