package com.example.digital_wallet.redis.service;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class IdempotencyService {

    StringRedisTemplate redisTemplate;

    static String PREFIX = "idempotency:transaction:";

    public boolean tryAcquire(String idempotencyKey)
    {
        String key = PREFIX + idempotencyKey;

        Boolean success = redisTemplate
                .opsForValue()
                .setIfAbsent(
                        key,
                        "PROCESSING",
                        Duration.ofSeconds(30)
                );

        return Boolean.TRUE.equals(success);

    }

    public void markCompleted(
            String idempotencyKey,
            String transactionId
    ) {

        String key = PREFIX + idempotencyKey;

        redisTemplate.opsForValue()
                .set(
                        key,
                        transactionId,
                        Duration.ofHours(24)
                );
    }



    public String getValue(String idempotencyKey) {

        return redisTemplate
                .opsForValue()
                .get(PREFIX + idempotencyKey);
    }
}
