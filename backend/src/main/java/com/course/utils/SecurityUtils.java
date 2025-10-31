package com.course.utils;

import com.course.entity.User;
import com.course.common.exception.ServiceException;
import com.course.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户对象
     * @throws ServiceException 如果用户未登录
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ServiceException("用户未登录");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getUser();
        }
        throw new ServiceException("用户未登录或会话已过期");
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     * @throws ServiceException 如果用户未登录
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * 检查当前用户是否为指定用户
     *
     * @param userId 要检查的用户ID
     * @return 如果当前用户是指定用户则返回true
     */
    public static boolean isCurrentUser(Long userId) {
        try {
            return userId != null && getCurrentUserId().equals(userId);
        } catch (ServiceException e) {
            return false;
        }
    }

    /**
     * 检查当前用户是否有指定角色
     *
     * @param role 角色名称
     * @return 如果当前用户具有指定角色则返回true
     */
    public static boolean hasRole(String role) {
        try {
            User user = getCurrentUser();
            return user != null && role.equals(user.getRole());
        } catch (ServiceException e) {
            return false;
        }
    }

    /**
     * 检查当前用户是否已认证
     *
     * @return 如果当前用户已认证则返回true
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
