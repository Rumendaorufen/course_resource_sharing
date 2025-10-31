package com.course.common.aspect;

import com.course.common.annotation.CheckResourceOwner;
import com.course.common.annotation.RequireRole;
import com.course.common.exception.ServiceException;
import com.course.entity.Assignment;
import com.course.entity.User;
import com.course.security.SecurityUtils;
import com.course.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAspect {

    private final AssignmentService assignmentService;

    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("未登录");
        }
        
        String userRole = currentUser.getRole().toUpperCase();
        String[] requiredRoles = requireRole.value();
        
        boolean hasRole = false;
        for (String role : requiredRoles) {
            if (userRole.equals(role.toUpperCase())) {
                hasRole = true;
                break;
            }
        }
        
        if (!hasRole) {
            throw new ServiceException("无权限执行此操作");
        }
    }

    @Before("@annotation(checkOwner)")
    public void checkResourceOwner(JoinPoint joinPoint, CheckResourceOwner checkOwner) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("未登录");
        }

        String parameterName = checkOwner.parameterName();
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();

        Long resourceId = null;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(parameterName)) {
                resourceId = (Long) args[i];
                break;
            }
        }

        if (resourceId == null) {
            throw new ServiceException("资源ID不能为空");
        }

        // 根据资源类型和ID检查当前用户是否为资源所有者
        boolean isOwner = checkResourceOwnership(currentUser.getId(), resourceId, checkOwner.resourceType());
        if (!isOwner) {
            throw new ServiceException("无权访问此资源");
        }
    }

    private boolean checkResourceOwnership(Long userId, Long resourceId, String resourceType) {
        // 根据不同的资源类型检查所有权
        switch (resourceType.toUpperCase()) {
            case "COURSE":
                return checkCourseOwnership(userId, resourceId);
            case "RESOURCE":
                return checkResourceOwnership(userId, resourceId);
            case "HOMEWORK":
            case "ASSIGNMENT":
                return checkAssignmentOwnership(userId, resourceId);
            default:
                return false;
        }
    }

    private boolean checkCourseOwnership(Long userId, Long courseId) {
        // TODO: 实现课程所有权检查
        return true;
    }

    private boolean checkResourceOwnership(Long userId, Long resourceId) {
        // TODO: 实现资源所有权检查
        return true;
    }

    private boolean checkAssignmentOwnership(Long userId, Long assignmentId) {
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        if (assignment == null) {
            return false;
        }
        return assignment.getTeacherId().equals(userId);
    }
}
