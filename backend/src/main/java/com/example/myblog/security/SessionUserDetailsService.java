package com.example.myblog.security;

import com.example.myblog.mapper.UserAccountMapper;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SessionUserDetailsService implements UserDetailsService {

    private final UserAccountMapper userAccountMapper;

    public SessionUserDetailsService(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public @NullMarked UserDetails loadUserByUsername(String username) {
        var user = userAccountMapper.findByAccount(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        return User.withUsername(user.getAccount())
                .password(user.getPasswordHash())
                .roles("USER")
                .build();
    }
}
