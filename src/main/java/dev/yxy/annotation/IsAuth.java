package dev.yxy.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 查看原码发现，ROLE_的前缀不一定要加
 * Created by Nuclear on 2021/1/26
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('ROLE_USER','ADMIN')")
public @interface IsAuth {
}
