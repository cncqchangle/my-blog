package com.example.myblog.service;

import com.example.myblog.domain.UserAccount;
import com.example.myblog.domain.exception.ConflictException;
import com.example.myblog.domain.exception.UnauthorizedException;
import com.example.myblog.domain.view.LoginResponse;
import com.example.myblog.mapper.UserAccountMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       SecurityContextRepository securityContextRepository,
                       UserAccountMapper userAccountMapper,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userAccountMapper = userAccountMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(String account,
                               String password,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        try {
            var authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(account, password));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
            return new LoginResponse(authentication.getName());
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new UnauthorizedException("Invalid account or password: " + ex.getMessage());
        }
    }

    @Transactional
    public LoginResponse register(String account,
                                  String password,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        String normalizedAccount = account.trim();
        if (userAccountMapper.findByAccount(normalizedAccount).isPresent()) {
            throw new ConflictException("Account already exists");
        }

        var now = LocalDateTime.now();
        UserAccount userAccount = new UserAccount();
        userAccount.setAccount(normalizedAccount);
        userAccount.setPasswordHash(passwordEncoder.encode(password));
        userAccount.setCreatedAt(now);
        userAccount.setUpdatedAt(now);
        try {
            userAccountMapper.insert(userAccount);
        } catch (DuplicateKeyException ex) {
            throw new ConflictException("Account already exists");
        }

        return login(normalizedAccount, password, request, response);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
