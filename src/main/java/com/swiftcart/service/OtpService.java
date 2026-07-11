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
        
        String rateKey = "otp:rate:" + phone;
        String countStr = redisService.get(rateKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);

        if (count >= 3) {
            throw new RuntimeException("OTP request limit exceeded for this hour. Maximum 3 requests allowed.");
        }

        redisService.incrementAndExpire(rateKey, Duration.ofHours(1));

        String otp = String.format("%06d", random.nextInt(1000000));

        String otpKey = "otp:" + phone;
        redisService.set(otpKey, otp, Duration.ofMinutes(10));

        return otp;
    }

    public boolean verifyOtp(String phone, String otp) {
        String attemptsKey = "otp:attempts:" + phone;
        String attemptsStr = redisService.get(attemptsKey);
        int attempts = attemptsStr == null ? 0 : Integer.parseInt(attemptsStr);

        if (attempts >= 5) {
            throw new RuntimeException("Maximum OTP verification attempts exceeded. Please generate a new OTP.");
        }

        String otpKey = "otp:" + phone;
        String storedOtp = redisService.get(otpKey);

        if (storedOtp == null) {
            redisService.incrementAndExpire(attemptsKey, Duration.ofMinutes(10));
            return false;
        }

        boolean matches = java.security.MessageDigest.isEqual(
                storedOtp.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                otp.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        if (matches) {
            redisService.delete(otpKey);
            redisService.delete(attemptsKey);
            return true;
        } else {
            redisService.incrementAndExpire(attemptsKey, Duration.ofMinutes(10));
            return false;
        }
    }
}
