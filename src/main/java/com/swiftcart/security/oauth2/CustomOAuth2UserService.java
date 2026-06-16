package com.swiftcart.security.oauth2;

import com.swiftcart.enums.Role;
import com.swiftcart.entity.User;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.security.CustomUserPrincipal;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            throw new RuntimeException("Email not found from OAuth2 provider: " + registrationId);
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            String existingProvider = user.getProvider();
            if (existingProvider == null || existingProvider.equalsIgnoreCase("LOCAL")) {
                user.setProvider(registrationId);
                user.setProviderId(oAuth2UserInfo.getId());
                if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
                    user.setAvatarUrl(oAuth2UserInfo.getImageUrl());
                }
                user = userRepository.save(user);
            } else if (!existingProvider.equalsIgnoreCase(registrationId)) {
                throw new org.springframework.security.authentication.InternalAuthenticationServiceException(
                    "Email already registered with " + existingProvider.toUpperCase() + ". Use that to login."
                );
            } else {
                user = updateExistingUser(user, oAuth2UserInfo);
            }
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return new CustomUserPrincipal(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = User.builder()
                .email(oAuth2UserInfo.getEmail())
                .name(oAuth2UserInfo.getName())
                .role(Role.CUSTOMER)
                .isVerified(true)
                .build();
                
        user.setProvider(oAuth2UserRequest.getClientRegistration().getRegistrationId());
        user.setProviderId(oAuth2UserInfo.getId());
        user.setAvatarUrl(oAuth2UserInfo.getImageUrl());
        user.setEmailVerified(true);

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setName(oAuth2UserInfo.getName());
        existingUser.setAvatarUrl(oAuth2UserInfo.getImageUrl());
        return userRepository.save(existingUser);
    }
}
