package dev.yxy.util;

import dev.yxy.model.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nuclear on 2021/1/27
 */
public class DBUtil {

    private static ConcurrentHashMap<String, List<String>> table = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, UserDetails> document = new ConcurrentHashMap<>();

    static {
        table.put("jack", List.of("jack", "123456", "ADMIN"));
        table.put("rose", List.of("rose", "123456", "USER"));
        table.put("tony", List.of("tony", "123456", "VISITOR"));

        document.put("jack", Member.builder().username("jack").password("123456").roles("ADMIN,USER").build());
        document.put("rose", Member.builder().username("rose").password("123456").roles("USER").build());
        document.put("tony", Member.builder().username("tony").password("123456").roles("VISITOR").build());
    }

    @Nullable
    public static List<String> getUser(@NotNull String username) {
        return table.get(username);
    }

    @Nullable
    public static UserDetails getUserDetails(@NotNull String username) {
        return document.get(username);
    }
}
