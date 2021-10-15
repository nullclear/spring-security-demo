package dev.yxy.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NOTE - 2021/10/15 ROLE_的前缀不一定要加
 * 查看源码发现，ROLE_的前缀不一定要加
 * Created by Nuclear on 2021/1/26
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ROLE_ADMIN')")
public @interface IsAdmin {
}
