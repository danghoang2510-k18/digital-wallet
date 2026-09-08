package com.example.digital_wallet.common.rate_limit;

import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.common.security.CurrentUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class RateLimitFilter extends OncePerRequestFilter {

     RateLimitService rateLimitService;
     RateLimitProperties rateLimitProperties;
     CurrentUserService currentUserService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.equals("/wallet/transfer")
                && "POST".equalsIgnoreCase(request.getMethod())) {

            checkUserRateLimit(
                    request,
                    rateLimitProperties.getTransfer()
            );
        }

        if (path.equals("/wallet/topup")
                && "POST".equalsIgnoreCase(request.getMethod())) {

            checkUserRateLimit(
                    request,
                    rateLimitProperties.getTopUp()
            );
        }

        filterChain.doFilter(request, response);
    }

    private void checkUserRateLimit(
            HttpServletRequest request,
            RateLimitProperties.Limit limit
    ) {



        String username = currentUserService.getCurrentUser().getUsername();

        String key =
                "rate_limit:user:"
                        + username
                        + ":"
                        + request.getRequestURI();

        boolean allowed =
                rateLimitService.isAllowed(
                        key,
                        limit.getMaxRequests(),
                        Duration.ofSeconds(
                                limit.getWindowSeconds()
                        )
                );

        if (!allowed) {
            throw new AppException(
                    ErrorCode.RATE_LIMIT_EXCEEDED
            );
        }
    }
}
