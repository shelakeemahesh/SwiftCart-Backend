package com.swiftcart.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpService {

    private final RedisFallbackService redisService;
    private final SecureRandom random = new SecureRandom();

    public OtpService(RedisFallbackService redisService) {
        this.redisService = redisService;
    }

    public String generateOtp(String phone) {
        // Check rate limiting (max 3 OTP requests per phone per hour)
        String rateKey = "otp:rate:" + phone;
        String countStr = redisService.get(rateKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);

        if (count >= 3) {
            throw new RuntimeException("OTP request limit exceeded for this hour. Maximum 3 requests allowed.");
        }

        // Increment count
        redisService.incrementAndExpire(rateKey, Duration.ofHours(1));

        // Generate 6 digit OTP
        String otp = String.format("%06d", random.nextInt(1000000));

        // Store with TTL 10 minutes
        String otpKey = "otp:" + phone;
        redisService.set(otpKey, otp, Duration.ofMinutes(10));

        return otp;
    }

    public boolean verifyOtp(String phone, String otp) {
        String otpKey = "otp:" + phone;
        String storedOtp = redisService.get(otpKey);

        if (storedOtp != null && storedOtp.equals(otp)) {
            redisService.delete(otpKey);
            return true;
        }
        return false;
    }
}
