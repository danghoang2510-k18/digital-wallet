package com.example.digital_wallet.redis.service;


import com.example.digital_wallet.transaction.entity.TransactionType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class IdempotencyService {

    StringRedisTemplate redisTemplate;

    static String PREFIX = "idempotency:transaction:";

    public boolean tryAcquire(String idempotencyKey,
                              UUID userId,
                              TransactionType type)
    {
        String key = PREFIX
                + type + ":"
                + userId + ":"
                + idempotencyKey
                ;

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
            UUID userId,
            TransactionType type,
            String transactionId
    ) {

        String key = PREFIX
                + type + ":"
                + userId + ":"
                + idempotencyKey
                ;

        redisTemplate.opsForValue()
                .set(
                        key,
                        transactionId,
                        Duration.ofHours(24)
                );
    }



    public String getValue(String idempotencyKey,
                           UUID userId,
                           TransactionType type) {
        String key = PREFIX
                + type + ":"
                + userId + ":"
                + idempotencyKey
                ;

        return redisTemplate
                .opsForValue()
                .get(key);
    }

    public void removeValue(String idempotencyKey,
                             UUID userId,
                             TransactionType type)
    {
        String key = PREFIX
                + type + ":"
                + userId + ":"
                + idempotencyKey
                ;
        redisTemplate.delete(key);
    }


}
