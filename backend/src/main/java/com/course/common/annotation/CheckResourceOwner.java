package com.course.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckResourceOwner {
    String parameterName() default "id";
    String resourceType() default "";
}
