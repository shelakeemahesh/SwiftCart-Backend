package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;
import com.swiftcart.exception.BadRequestException;
import com.swiftcart.exception.UnauthorizedException;

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
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.verifyOtp(request.getPhone(), request.getOtp());
        setRefreshTokenCookie(httpRequest, response, authResponse.getRefreshToken());
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
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        setRefreshTokenCookie(httpRequest, response, authResponse.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response,
            @CookieValue(name = "refreshToken", required = false) String refreshTokenFromCookie,
            @RequestBody(required = false) Map<String, String> body) {

        String token = refreshTokenFromCookie;
        if (token == null && body != null) {
            token = body.get("refreshToken");
        }
        if (token == null) {
            token = request.getParameter("refreshToken");
        }

        if (token == null || token.isBlank()) {
            throw new BadRequestException("Refresh token is missing");
        }

        AuthResponse authResponse = authService.refreshToken(token);
        setRefreshTokenCookie(request, response, authResponse.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenFromCookie,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request,
            HttpServletResponse response) {
        String token = refreshTokenFromCookie;
        if (token == null && body != null) {
            token = body.get("refreshToken");
        }
        if (token == null) {
            token = request.getParameter("refreshToken");
        }
        authService.logout(token);
        clearRefreshTokenCookie(request, response);
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

    private void setRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        boolean isHttps = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(isHttps) 
                .path("/")
                .maxAge(7 * 24 * 60 * 60) 
                .sameSite(isHttps ? "None" : "Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response) {
        boolean isHttps = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(isHttps)
                .path("/")
                .maxAge(0)
                .sameSite(isHttps ? "None" : "Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(java.security.Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        UserResponse userResponse = authService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }
}
