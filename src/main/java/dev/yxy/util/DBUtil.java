package dev.yxy.util;

import dev.yxy.model.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nuclear on 2021/1/27
 */
public class DBUtil {

    private static ConcurrentHashMap<String, List<String>> table = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, UserDetails> document = new ConcurrentHashMap<>();

    private static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    static {
        table.put("jack", List.of("jack", "123456", "ADMIN"));
        table.put("rose", List.of("rose", "123456", "USER"));
        table.put("tony", List.of("tony", "123456", "VISITOR"));

        document.put("jack", Member.builder().username("jack").password("123456").roles("ADMIN,USER").passwordEncoder(s -> passwordEncoder.encode(s)).build());
        document.put("rose", Member.builder().username("rose").password("123456").roles("USER").passwordEncoder(s -> passwordEncoder.encode(s)).build());
        document.put("tony", Member.builder().username("tony").password("123456").roles("VISITOR").passwordEncoder(s -> passwordEncoder.encode(s)).build());
        document.put("mike", Member.builder().username("mike").password("123456").roles("ADMIN").passwordEncoder(s -> passwordEncoder.encode(s)).build());
        document.put("tom", Member.builder().username("tom").password("123456").roles("USER").accountLocked(true).passwordEncoder(s -> passwordEncoder.encode(s)).build());
    }

    @Nullable
    public static List<String> getUser(@NotNull String username) {
        return table.get(username);
    }

    @Nullable
    public static UserDetails getUserDetails(@NotNull String username) {
        UserDetails userDetails = document.get(username);
        if (userDetails == null) {
            return null;
        } else {
            // 这里如果不复制一遍，等会原始对象因为在同一个内存里，密码会被擦掉，第二次就没法登录了
            Member member = (Member) userDetails;
            Member copy = new Member();
            copy.setUsername(member.getUsername());
            copy.setPassword(member.getPassword());
            copy.setRoles(member.getRoles());
            copy.setAccountExpired(!member.isAccountNonExpired());
            copy.setAccountLocked(!member.isAccountNonLocked());
            copy.setCredentialsExpired(!member.isCredentialsNonExpired());
            copy.setDisabled(!member.isEnabled());
            return copy;
        }
    }
}
