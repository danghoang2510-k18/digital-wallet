package com.example.digital_wallet.common.rate_limit;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class RateLimitService {

    private StringRedisTemplate redisTemplate;


    private static final RedisScript<Long> RATE_LIMIT_SCRIPT =
            RedisScript.of(
                    """
                    local current = redis.call('INCR', KEYS[1])

                    if current == 1 then
                        redis.call('EXPIRE', KEYS[1], ARGV[1])
                    end

                    return current
                    """,
                    Long.class
            );

    public boolean isAllowed(
            String key,
            int maxRequests,
            Duration window
    ) {

        Long currentCount = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(window.getSeconds())
        );

        return currentCount != null
                && currentCount <= maxRequests;
    }
}
