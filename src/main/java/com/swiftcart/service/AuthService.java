package com.swiftcart.service;

import com.swiftcart.dto.request.*;
import com.swiftcart.dto.response.*;
import com.swiftcart.enums.Role;
import com.swiftcart.entity.User;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Value("${app.frontend.domain:http://localhost:5173}")
    private String frontendDomain;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final NotificationService notificationService;
    private final RedisFallbackService redisService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            OtpService otpService,
            NotificationService notificationService,
            RedisFallbackService redisService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.notificationService = notificationService;
        this.redisService = redisService;
    }

    public void sendOtp(String phone) {
        String otp = otpService.generateOtp(phone);
        
        // Persist OTP expiry & code in MySQL User model as secondary metadata if user exists
        userRepository.findByPhone(phone).ifPresent(user -> {
            user.setOtp(otp);
            user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);

            // Send OTP via email if user has a registered email address
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                notificationService.sendEmail(
                    user.getEmail(),
                    "SwiftCart - Your Verification OTP",
                    "Dear " + user.getName() + ",\n\nYour SwiftCart verification OTP is: " + otp + ". Valid for 10 minutes.\n\nThank you for shopping with SwiftCart! ⚡"
                );
            }
        });

        notificationService.sendOtpSms(phone, otp);
    }

    public AuthResponse verifyOtp(String phone, String otp) {
        boolean valid = otpService.verifyOtp(phone, otp);
        if (!valid) {
            // Check fallback in MySQL user record
            User user = userRepository.findByPhone(phone)
                    .orElseThrow(() -> new RuntimeException("Verification failed. Incorrect OTP or user does not exist."));
            if (user.getOtp() != null && user.getOtp().equals(otp) && user.getOtpExpiresAt().isAfter(LocalDateTime.now())) {
                valid = true;
                user.setOtp(null);
                user.setOtpExpiresAt(null);
                userRepository.save(user);
            }
        }

        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found after OTP verification"));

        if (!user.isVerified()) {
            user.setVerified(true);
            userRepository.save(user);
        }

        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getIdentifier())
                .or(() -> userRepository.findByEmail(request.getIdentifier()))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isVerified()) {
            throw new RuntimeException("Account is not verified. Please verify using OTP.");
        }

        return generateAuthResponse(user);
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email address already registered");
        }

        User user = User.builder()
                .phone(request.getPhone())
                .email(request.getEmail())
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .isVerified(false)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public void registerSeller(SellerRegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email address already registered");
        }

        User user = User.builder()
                .phone(request.getPhone())
                .email(request.getEmail())
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.SELLER)
                .isVerified(false)
                .businessName(request.getBusinessName())
                .gstin(request.getGstin())
                .panNumber(request.getPanNumber())
                .pickupAddress(request.getPickupAddress())
                .pickupPincode(request.getPickupPincode())
                .build();

        userRepository.save(user);
    }

    public AuthResponse refreshToken(String oldRefreshToken) {
        if (oldRefreshToken == null || !jwtUtil.validateTokenOnly(oldRefreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtUtil.extractUsername(oldRefreshToken);
        String redisKey = "refresh:" + username;
        String storedRefreshToken = redisService.get(redisKey);

        if (storedRefreshToken == null || !storedRefreshToken.equals(oldRefreshToken)) {
            throw new RuntimeException("Refresh token is invalid or has been rotated");
        }

        User user = userRepository.findByPhone(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Refresh token rotation: invalidate old one and issue a new pair
        redisService.delete(redisKey);
        return generateAuthResponse(user);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null && jwtUtil.validateTokenOnly(refreshToken)) {
            String username = jwtUtil.extractUsername(refreshToken);
            redisService.delete("refresh:" + username);
        }
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        String redisKey = "reset:" + token;
        // Store mapping from reset-token to email with 15-minute TTL
        redisService.set(redisKey, email, Duration.ofMinutes(15));

        String resetLink = frontendDomain + "/reset-password?token=" + token;
        notificationService.sendEmail(email, "SwiftCart - Password Reset Request",
                "Hello " + user.getName() + ",\n\nYou requested a password reset. Click the link below to set a new password:\n" +
                resetLink + "\n\nNote: This link expires in 15 minutes.\n\nSwiftCart Team ⚡");
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        String redisKey = "reset:" + token;
        String email = redisService.get(redisKey);

        if (email == null) {
            throw new RuntimeException("Password reset token is invalid or has expired");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redisService.delete(redisKey);
    }

    public AuthResponse generateAuthResponse(User user) {
        String username = user.getPhone() != null ? user.getPhone() : user.getEmail();
        String accessToken = jwtUtil.generateAccessToken(username, user.getId(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(username);

        // Store refresh token with 7-day TTL
        String redisKey = "refresh:" + username;
        redisService.set(redisKey, refreshToken, Duration.ofDays(7));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .phone(user.getPhone())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByPhone(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getProvider(),
                user.getAvatarUrl()
        );
    }
}
