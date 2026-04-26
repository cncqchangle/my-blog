package com.example.myblog.controller;

import com.example.myblog.domain.command.LoginRequest;
import com.example.myblog.domain.command.RegisterRequest;
import com.example.myblog.domain.view.LoginResponse;
import com.example.myblog.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request,
                               HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse) {
        return authService.login(request.account(), request.password(), httpServletRequest, httpServletResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request,
                                                  HttpServletRequest httpServletRequest,
                                                  HttpServletResponse httpServletResponse) {
        LoginResponse response = authService.register(
                request.account(),
                request.password(),
                httpServletRequest,
                httpServletResponse);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }
}
