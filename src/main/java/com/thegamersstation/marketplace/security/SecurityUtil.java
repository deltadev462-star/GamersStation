package com.thegamersstation.marketplace.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for accessing security context information
 */
public class SecurityUtil {

    /**
     * Get current authenticated user
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }
        
        return null;
    }

    /**
     * Get current user ID
     */
    public static Long getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Get current user phone number
     */
    public static String getCurrentUserPhone() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getPhoneNumber() : null;
    }

    /**
     * Get current user role
     */
    public static String getCurrentUserRole() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * Check if current user has role
     */
    public static boolean hasRole(String role) {
        String currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equals(role);
    }

    /**
     * Check if current user is admin
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is store manager
     */
    public static boolean isStoreManager() {
        return hasRole("STORE_MANAGER");
    }

    /**
     * Check if current user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }
}
