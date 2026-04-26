package com.example.myblog.domain.command;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "Account is required") String account,
        @NotBlank(message = "Password is required") String password) {
}

