package com.example.skill_management.security;

import com.example.skill_management.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final String username;
    private final String password;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.username = user.getEmail();
        this.password = user.getPassword();

        if ("ADMIN".equals(user.getRole())) {
            this.authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("write_user"),
                    new SimpleGrantedAuthority("delete_user"),
                    new SimpleGrantedAuthority("read_user")
            );
        } else if ("USER".equals(user.getRole())) {
            this.authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("read_user")
            );
        } else if ("TECHNICAL_QUALITY_MANAGER".equals(user.getRole())) {
            this.authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_TECHNICAL_QUALITY_MANAGER")
            );
        } else {
            this.authorities = List.of();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
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
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}