package com.royalpearl.hotel.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Convenience helpers for accessing the current authenticated principal.
 */
public final class SecurityUtil {

    private SecurityUtil() {}

    /** Returns the UUID of the currently authenticated user, if any. */
    public static Optional<UUID> currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        Object principal = auth.getPrincipal();
        if (principal instanceof UUID uuid) return Optional.of(uuid);
        return Optional.empty();
    }

    /** Returns true if the current user has the ROLE_admin authority. */
    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_admin".equals(a.getAuthority()));
    }

    /** Throws if no user is authenticated. */
    public static UUID requireCurrentUserId() {
        return currentUserId()
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException(
                        "Authentication required"));
    }
}
