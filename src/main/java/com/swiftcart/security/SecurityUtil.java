package com.swiftcart.security;

import com.swiftcart.entity.User;
import com.swiftcart.repository.UserRepository;
import org.springframework.security.core.Authentication;
import java.security.Principal;

public class SecurityUtil {

    public static User getUserFromPrincipal(Principal principal, UserRepository userRepository) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized: No principal provided");
        }
        
        if (principal instanceof Authentication) {
            Object principalObj = ((Authentication) principal).getPrincipal();
            if (principalObj instanceof CustomUserPrincipal) {
                return userRepository.findById(((CustomUserPrincipal) principalObj).getUser().getId())
                        .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
            }
        }
        
        String username = principal.getName();
        try {
            Long id = Long.parseLong(username);
            return userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        } catch (NumberFormatException e) {
            return userRepository.findByPhone(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        }
    }
}
