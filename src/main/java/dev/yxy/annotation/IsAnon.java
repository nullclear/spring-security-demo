package dev.yxy.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NOTE - 2021/10/15 匿名与认证是互斥状态
 * 这个不能使用isAnonymous()，因为登录了的不算匿名
 * Created by Nuclear on 2021/1/26
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("permitAll()")
public @interface IsAnon {
}
