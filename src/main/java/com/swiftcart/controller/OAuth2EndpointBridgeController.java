package com.swiftcart.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

/**
 * Bridges standard Spring Security OAuth2 endpoints and custom configured endpoints
 * to prevent redirect URI mismatches and ensure compatibility.
 */
@Controller
public class OAuth2EndpointBridgeController {

    @GetMapping("/oauth2/authorize/{registrationId}")
    public void forwardOAuth2Authorize(
            @PathVariable String registrationId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String query = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        response.sendRedirect("/oauth2/authorization/" + registrationId + query);
    }

    @GetMapping("/login/oauth2/code/{registrationId}")
    public void forwardOAuth2Callback(
            @PathVariable String registrationId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String query = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        response.sendRedirect("/oauth2/callback/" + registrationId + query);
    }
}
