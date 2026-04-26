package com.example.myblog.domain.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFolderRequest(
        @NotBlank(message = "Folder name is required")
        @Size(max = 100, message = "Folder name must be at most 100 characters")
        String name) {
}

