package com.swiftcart.security.oauth2;

import com.swiftcart.entity.User;
import com.swiftcart.enums.Role;
import com.swiftcart.security.CustomUserPrincipal;
import com.swiftcart.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OAuth2FlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Autowired
    private AuthService authService;

    @Test
    public void testOAuth2AuthorizationEndpointRedirects() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("accounts.google.com")));
    }

    @Test
    public void testOAuth2BridgeAuthorizeRedirectsToStandardEndpoint() throws Exception {
        mockMvc.perform(get("/oauth2/authorize/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/oauth2/authorization/google")));
    }

    @Test
    public void testOAuth2SuccessHandlerGeneratesTokensAndRedirect() throws Exception {
        User user = User.builder()
                .name("OAuth Customer")
                .email("oauth_customer@swiftcart.com")
                .role(Role.CUSTOMER)
                .isVerified(true)
                .build();
        user.setId(999L);

        CustomUserPrincipal principal = new CustomUserPrincipal(user, Map.of("email", user.getEmail(), "name", user.getName()));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, authentication);

        String redirectedUrl = response.getRedirectedUrl();
        assertNotNull(redirectedUrl);
        assertTrue(redirectedUrl.contains("token="));
        assertTrue(redirectedUrl.contains("refreshToken="));
        assertTrue(redirectedUrl.contains("/oauth2/callback"));
    }
}
