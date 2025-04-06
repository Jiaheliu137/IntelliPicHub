package com.jiahe.intellipichub.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
// 用这个注解的地方代表必须要用户登录，如果不设置权限就不用这个注解
public @interface AuthCheck {


    /**
     * 必须具有某个角色
     * @return
     */
    String mustRole() default "";

}
