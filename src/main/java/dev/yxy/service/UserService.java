package dev.yxy.service;

import dev.yxy.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME;

/**
 * Created by Nuclear on 2021/1/27
 */
@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisIndexedSessionRepository redisIndexedSessionRepository;

    @Autowired
    private JdbcTokenRepositoryImpl jdbcTokenRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("申请登录的用户名：{}", username);
        if (username == null) {
            throw new UsernameNotFoundException("非法用户名");
        }
        return deluxe(username);
    }

    private UserDetails common(String username) {
        List<String> list = DBUtil.getUser(username);
        if (list == null) {
            throw new UsernameNotFoundException("不存在的用户");
        }
        return User.withUsername(list.get(0)).password(list.get(1)).roles(list.get(2))
                .passwordEncoder(s -> passwordEncoder.encode(s)) //假如数据库里存的是明文，可以用此方法生成密文，然后与前端传过来的加密过的密码比对
                .build();
    }

    private UserDetails deluxe(String username) {
        UserDetails userDetails = DBUtil.getUserDetails(username);
        if (userDetails == null) {
            throw new UsernameNotFoundException("不存在的用户");
        }
        return userDetails;
    }

    public Map<String, ? extends Session> findByIndexNameAndIndexValue(String username) {
        return redisIndexedSessionRepository.findByIndexNameAndIndexValue(PRINCIPAL_NAME_INDEX_NAME, username);
    }

    public int deleteOther(String id, String username) throws Exception {
        int count = 0;
        Map<String, ? extends Session> map = redisIndexedSessionRepository.findByIndexNameAndIndexValue(PRINCIPAL_NAME_INDEX_NAME, username);
        for (String sessionId : map.keySet()) {
            if (!Objects.equals(sessionId, id)) {
                redisIndexedSessionRepository.deleteById(sessionId);
                count++;
            }
        }
        // 可能还存在remember-me的用户，这个不删除等会还能登录
        jdbcTokenRepository.removeUserTokens(username);
        return count;
    }
}
