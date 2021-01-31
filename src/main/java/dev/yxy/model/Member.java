package dev.yxy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Nuclear on 2021/1/27
 */
public class Member implements UserDetails, CredentialsContainer {
    private static final long serialVersionUID = 6703014957557809028L;

    private String username = "";
    @JsonIgnore
    private String password = "";
    private String roles = "";
    private boolean accountExpired;
    private boolean accountLocked;
    private boolean credentialsExpired;
    private boolean disabled;
    // 版本号
    private int version;

    public Member() {
    }

    private Member(Builder builder) {
        setUsername(builder.username);
        setPassword(builder.password);
        setRoles(builder.roles);
        setAccountExpired(builder.accountExpired);
        setAccountLocked(builder.accountLocked);
        setCredentialsExpired(builder.credentialsExpired);
        setDisabled(builder.disabled);
    }

    public static Builder builder() {
        return new Builder();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public void setAccountExpired(boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public void setCredentialsExpired(boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    //Jackson 只有getter也会被json化，如果不想要就去掉吧
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        String[] arr = roles.split(",");
        for (String role : arr) {
            if (role.startsWith("ROLE_")) {
                authorities.add(new SimpleGrantedAuthority(role));
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !accountExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return !disabled;
    }

    @Override
    public void eraseCredentials() {
        password = null;
    }

    @Override
    public String toString() {
        return "{"
                + "\"username\":\""
                + username + '\"'
                + ",\"roles\":\""
                + roles + '\"'
                + ",\"accountExpired\":"
                + accountExpired
                + ",\"accountLocked\":"
                + accountLocked
                + ",\"credentialsExpired\":"
                + credentialsExpired
                + ",\"disabled\":"
                + disabled
                + "}";
    }

    public static final class Builder {
        private String username;
        private String password;
        private String roles;
        private boolean accountExpired;
        private boolean accountLocked;
        private boolean credentialsExpired;
        private boolean disabled;
        private Function<String, String> passwordEncoder = password -> password;

        private Builder() {
        }

        public Builder username(String username) {
            Assert.notNull(username, "username cannot be null");
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            Assert.notNull(password, "password cannot be null");
            this.password = password;
            return this;
        }

        public Builder passwordEncoder(Function<String, String> encoder) {
            Assert.notNull(encoder, "encoder cannot be null");
            this.passwordEncoder = encoder;
            return this;
        }

        public Builder roles(String... role) {
            StringBuilder builder = new StringBuilder();
            int max = role.length - 1;
            for (int i = 0; i < role.length; i++) {
                builder.append(role[i]);
                if (i == max) break;
                builder.append(",");
            }
            this.roles = builder.toString();
            return this;
        }

        public Builder accountExpired(boolean accountExpired) {
            this.accountExpired = accountExpired;
            return this;
        }

        public Builder accountLocked(boolean accountLocked) {
            this.accountLocked = accountLocked;
            return this;
        }

        public Builder credentialsExpired(boolean credentialsExpired) {
            this.credentialsExpired = credentialsExpired;
            return this;
        }

        public Builder disabled(boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        public Member build() {
            this.password = this.passwordEncoder.apply(password);
            return new Member(this);
        }
    }
}
