package com.course.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckResourceOwner {
    String parameterName();      // 资源ID参数名
    String resourceType();       // 资源类型：COURSE, RESOURCE, HOMEWORK
}
