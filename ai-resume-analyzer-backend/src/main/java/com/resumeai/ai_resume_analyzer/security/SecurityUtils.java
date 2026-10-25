package com.resumeai.ai_resume_analyzer.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Optional<String> getCurrentUserId() {
        return getCurrentUserPrincipal().map(UserPrincipal::getId);
    }

    public static boolean isCurrentUserGuest() {
        return getCurrentUserPrincipal().map(UserPrincipal::isGuest).orElse(false);
    }
}
