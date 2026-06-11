package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;

import com.swiftcart.dto.request.*;
import com.swiftcart.dto.response.*;
import com.swiftcart.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendOtp(@RequestParam String phone) {
        authService.sendOtp(phone);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "OTP sent successfully to phone " + phone)));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.verifyOtp(request.getPhone(), request.getOtp());
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "User registered successfully. Please verify your phone number via OTP.")));
    }

    @PostMapping("/register/seller")
    public ResponseEntity<ApiResponse<Map<String, String>>> registerSeller(@Valid @RequestBody SellerRegisterRequest request) {
        authService.registerSeller(request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Seller registered successfully. Please verify your phone number via OTP.")));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response,
            @CookieValue(name = "refreshToken", required = false) String refreshTokenFromCookie) {

        String token = refreshTokenFromCookie;
        if (token == null) {
            // Fallback to reading request parameter/body if cookies not enabled/supported
            token = request.getParameter("refreshToken");
        }

        if (token == null) {
            throw new RuntimeException("Refresh token is missing");
        }

        AuthResponse authResponse = authService.refreshToken(token);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken);
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Logged out successfully")));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Password reset link sent to your email")));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Password reset successfully")));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(java.security.Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }
        UserResponse userResponse = authService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }
}
