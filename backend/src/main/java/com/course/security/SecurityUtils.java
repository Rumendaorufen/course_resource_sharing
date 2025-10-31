package com.course.security;

import com.course.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtils {
    
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authentication found in SecurityContext");
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            log.debug("Retrieved current user: {}, ID: {}, Role: {}", 
                userDetails.getUsername(), 
                userDetails.getUser().getId(),
                userDetails.getUser().getRole());
            return userDetails.getUser();
        }
        
        log.warn("Principal is not an instance of UserDetailsImpl: {}", principal.getClass());
        return null;
    }
    
    public static Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            log.warn("Failed to get current user ID - user not authenticated");
            return null;
        }
        log.debug("Retrieved current user ID: {}", currentUser.getId());
        return currentUser.getId();
    }
    
    public static boolean isTeacher() {
        User currentUser = getCurrentUser();
        boolean isTeacher = currentUser != null && "TEACHER".equalsIgnoreCase(currentUser.getRole());
        log.debug("Checking if current user is teacher: {}", isTeacher);
        return isTeacher;
    }
    
    public static boolean isStudent() {
        User currentUser = getCurrentUser();
        boolean isStudent = currentUser != null && "STUDENT".equalsIgnoreCase(currentUser.getRole());
        log.debug("Checking if current user is student: {}", isStudent);
        return isStudent;
    }
    
    public static boolean isAdmin() {
        User currentUser = getCurrentUser();
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
        log.debug("Checking if current user is admin: {}", isAdmin);
        return isAdmin;
    }
}
