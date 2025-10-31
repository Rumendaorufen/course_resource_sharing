package com.course.security.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String[] value();
    boolean logical() default true; // true 表示 OR 逻辑，false 表示 AND 逻辑
}
