package com.course.security.aspect;

import com.course.common.exception.ServiceException;
import com.course.entity.Assignment;
import com.course.entity.Course;
import com.course.entity.Resource;
import com.course.mapper.AssignmentMapper;
import com.course.mapper.CourseMapper;
import com.course.mapper.ResourceMapper;
import com.course.security.UserDetailsImpl;
import com.course.security.annotation.CheckResourceOwner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ResourceOwnerCheckAspect {

    private final CourseMapper courseMapper;
    private final ResourceMapper resourceMapper;
    private final AssignmentMapper assignmentMapper;

    @Before("@annotation(com.course.security.annotation.CheckResourceOwner)")
    public void checkResourceOwner(JoinPoint joinPoint) {
        // 获取当前用户认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ServiceException("未登录或登录已过期");
        }

        // 获取用户信息
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();
        String userRole = userDetails.getUser().getRole();

        // 如果是管理员，直接放行
        if ("ADMIN".equals(userRole)) {
            return;
        }

        // 获取方法上的CheckResourceOwner注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckResourceOwner checkOwner = method.getAnnotation(CheckResourceOwner.class);

        // 获取资源ID
        String parameterName = checkOwner.parameterName();
        Long resourceId = getParameterValue(joinPoint, method, parameterName);
        if (resourceId == null) {
            throw new ServiceException("资源ID不能为空");
        }

        // 根据资源类型检查所有权
        boolean isOwner = false;
        String resourceType = checkOwner.resourceType();
        switch (resourceType) {
            case "COURSE":
                Course course = courseMapper.selectById(resourceId);
                isOwner = course != null && course.getTeacherId().equals(userId);
                break;
            case "RESOURCE":
                Resource resource = resourceMapper.selectById(resourceId);
                if (resource != null) {
                    Course resourceCourse = courseMapper.selectById(resource.getCourseId());
                    isOwner = resourceCourse != null && resourceCourse.getTeacherId().equals(userId);
                }
                break;
            case "HOMEWORK":
                Assignment assignment = assignmentMapper.selectById(resourceId);
                if (assignment != null) {
                    Course homeworkCourse = courseMapper.selectById(assignment.getCourseId());
                    isOwner = homeworkCourse != null && homeworkCourse.getTeacherId().equals(userId);
                }
                break;
            default:
                throw new ServiceException("未知的资源类型");
        }

        if (!isOwner) {
            log.warn("用户 {} 尝试访问不属于自己的资源 {}", userDetails.getUsername(), resourceId);
            throw new ServiceException("没有操作权限");
        }
    }

    private Long getParameterValue(JoinPoint joinPoint, Method method, String parameterName) {
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();
        
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(parameterName)) {
                Object arg = args[i];
                if (arg instanceof Long) {
                    return (Long) arg;
                } else if (arg instanceof String) {
                    return Long.parseLong((String) arg);
                } else if (arg instanceof Integer) {
                    return ((Integer) arg).longValue();
                }
            }
        }
        return null;
    }
}
