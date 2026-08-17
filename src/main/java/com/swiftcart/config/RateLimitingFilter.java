package com.swiftcart.config;

import com.swiftcart.service.RedisFallbackService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RedisFallbackService redisFallbackService;

    public RateLimitingFilter(RedisFallbackService redisFallbackService) {
        this.redisFallbackService = redisFallbackService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ip = getClientIp(request);
        String path = request.getRequestURI();

        boolean isAuthEndpoint = path.startsWith("/api/v1/auth");
        int limit = isAuthEndpoint ? 100 : 1000;
        String type = isAuthEndpoint ? "auth" : "public";

        long currentMinute = System.currentTimeMillis() / 60000;
        String rateKey = "rate:" + type + ":" + ip + ":" + currentMinute;

        try {
            Long count = redisFallbackService.incrementAndExpire(rateKey, Duration.ofSeconds(60));

            if (count != null && count > limit) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many requests. Limit is " + limit + " per minute for " + type + " endpoints.\"}");
                return;
            }
        } catch (Exception ignored) {}

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
