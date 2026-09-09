package com.example.usermanagement.config;

import com.example.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("securityCheck")
@RequiredArgsConstructor
public class SecurityCheck {
    private final UserRepository userRepository;
    public boolean isOwner(Authentication authentication, Long localUserId) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }
        // authentication.getName() mặc định chứa thuộc tính "sub" (Keycloak ID) của user
        String tokenKeycloakId = authentication.getName();
        return userRepository.findById(localUserId)
                .map(user -> user.getKeycloakId().equals(tokenKeycloakId))
                .orElse(false);
    }
}
