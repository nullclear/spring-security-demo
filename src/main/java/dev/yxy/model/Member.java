package dev.yxy.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Nuclear on 2021/1/27
 */
public class Member implements UserDetails {
    private static final long serialVersionUID = 6703014957557809028L;

    private String username = "";
    private String password = "";
    private String roles = "";
    private boolean accountExpired;
    private boolean accountLocked;
    private boolean credentialsExpired;
    private boolean disabled;

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
    public String toString() {
        return "{"
                + "\"username\":\""
                + username + '\"'
                + ",\"password\":\""
                + password + '\"'
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

        private Builder() {
        }

        public Builder username(String val) {
            username = val;
            return this;
        }

        public Builder password(String val) {
            password = val;
            return this;
        }

        public Builder roles(String... role) {
            StringBuilder b = new StringBuilder();
            int max = role.length - 1;
            for (int i = 0; i < role.length; i++) {
                b.append(role[i]);
                if (i == max) break;
                b.append(",");
            }
            roles = b.toString();
            return this;
        }

        public Builder accountExpired(boolean val) {
            accountExpired = val;
            return this;
        }

        public Builder accountLocked(boolean val) {
            accountLocked = val;
            return this;
        }

        public Builder credentialsExpired(boolean val) {
            credentialsExpired = val;
            return this;
        }

        public Builder disabled(boolean val) {
            disabled = val;
            return this;
        }

        public Member build() {
            return new Member(this);
        }
    }
}
