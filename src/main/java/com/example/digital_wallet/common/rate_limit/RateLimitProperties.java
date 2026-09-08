package com.example.digital_wallet.common.rate_limit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate-limit")
@Getter
@Setter
public class RateLimitProperties {


    private Limit transfer = new Limit();
    private Limit topUp = new Limit();

    @Getter
    @Setter
    public static class Limit {

        private int maxRequests;

        private long windowSeconds;
    }
}
