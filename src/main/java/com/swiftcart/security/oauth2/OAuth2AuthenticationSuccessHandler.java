package com.swiftcart.security.oauth2;

import com.swiftcart.dto.response.AuthResponse;
import com.swiftcart.security.CustomUserPrincipal;
import com.swiftcart.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Value("${app.oauth2.authorizedRedirectUris:http://localhost:5173/oauth2/callback,http://localhost:3000/oauth2/callback,http://localhost:4173/oauth2/callback,https://swiftcart.vercel.app/oauth2/callback}")
    private List<String> authorizedRedirectUris;

    @Value("${app.frontend.domain:http://localhost:5173}")
    private String frontendDomain;

    public OAuth2AuthenticationSuccessHandler(AuthService authService, HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository) {
        this.authService = authService;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl;
        try {
            targetUrl = determineTargetUrl(request, response, authentication);
        } catch (IllegalArgumentException ex) {
            String fallbackUrl = (authorizedRedirectUris != null && !authorizedRedirectUris.isEmpty())
                    ? authorizedRedirectUris.get(0).split(",")[0].trim().replace("\"", "").replace("'", "")
                    : "http://localhost:5173/oauth2/callback";
            targetUrl = UriComponentsBuilder.fromUriString(fallbackUrl)
                    .queryParam("error", ex.getMessage())
                    .build().toUriString();
        }

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new IllegalArgumentException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }

        String targetUrl = redirectUri.orElse(
                (authorizedRedirectUris != null && !authorizedRedirectUris.isEmpty())
                ? authorizedRedirectUris.get(0).split(",")[0].trim().replace("\"", "").replace("'", "")
                : "http://localhost:5173/oauth2/callback"
        );

        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        AuthResponse authResponse = authService.generateAuthResponse(userPrincipal.getUser());

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", authResponse.getAccessToken())
                .queryParam("refreshToken", authResponse.getRefreshToken())
                .build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        if (uri == null || uri.isBlank()) {
            return false;
        }
        try {
            URI clientRedirectUri = URI.create(uri.trim());
            String clientHost = clientRedirectUri.getHost();
            if (clientHost == null) return false;

            // Allow localhost/127.0.0.1 in local development
            if (clientHost.equalsIgnoreCase("localhost") || clientHost.equals("127.0.0.1")) {
                return true;
            }

            // Check against configured frontendDomain
            if (frontendDomain != null && !frontendDomain.isBlank()) {
                URI frontendURI = URI.create(frontendDomain.trim());
                if (frontendURI.getHost() != null && frontendURI.getHost().equalsIgnoreCase(clientHost)) {
                    return true;
                }
            }

            // Check against configured authorizedRedirectUris
            if (authorizedRedirectUris != null) {
                for (String rawUri : authorizedRedirectUris) {
                    if (rawUri == null) continue;
                    String[] splitUris = rawUri.split(",");
                    for (String singleUri : splitUris) {
                        String cleanUri = singleUri.trim().replace("\"", "").replace("'", "");
                        if (cleanUri.isBlank()) continue;
                        try {
                            URI authorizedURI = URI.create(cleanUri);
                            if (authorizedURI.getHost() != null && authorizedURI.getHost().equalsIgnoreCase(clientHost)) {
                                int authPort = authorizedURI.getPort() != -1 ? authorizedURI.getPort() : (authorizedURI.getScheme() != null && authorizedURI.getScheme().equalsIgnoreCase("https") ? 443 : 80);
                                int clientPort = clientRedirectUri.getPort() != -1 ? clientRedirectUri.getPort() : (clientRedirectUri.getScheme() != null && clientRedirectUri.getScheme().equalsIgnoreCase("https") ? 443 : 80);
                                if (authPort == clientPort) {
                                    return true;
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }
}
