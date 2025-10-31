package com.course.security.aspect;

import com.course.common.exception.ServiceException;
import com.course.security.UserDetailsImpl;
import com.course.security.annotation.RequireRole;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class RoleCheckAspect {

    @Before("@annotation(com.course.security.annotation.RequireRole)")
    public void checkRole(JoinPoint joinPoint) {
        // 获取当前用户认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ServiceException("未登录或登录已过期");
        }

        // 获取用户角色
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userRole = userDetails.getUser().getRole();

        // 获取方法上的RequireRole注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);

        // 检查角色权限
        String[] requiredRoles = requireRole.value();
        boolean logical = requireRole.logical();
        boolean hasPermission = logical
                ? Arrays.asList(requiredRoles).contains(userRole)  // OR逻辑
                : Arrays.asList(requiredRoles).contains(userRole); // AND逻辑（对于单一角色来说，OR和AND是一样的）

        if (!hasPermission) {
            log.warn("用户 {} 尝试访问需要角色 {} 的方法 {}", userDetails.getUsername(), 
                    Arrays.toString(requiredRoles), method.getName());
            throw new ServiceException("没有操作权限");
        }
    }
}
