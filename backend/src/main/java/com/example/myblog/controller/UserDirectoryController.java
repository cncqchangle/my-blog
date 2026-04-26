package com.example.myblog.controller;

import com.example.myblog.domain.view.SearchUserResponse;
import com.example.myblog.service.UserDirectoryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.security.Principal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserDirectoryController {

    private final UserDirectoryService userDirectoryService;

    public UserDirectoryController(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    @GetMapping("/search")
    public SearchUserResponse search(@RequestParam("q") @NotBlank String query,
                                     @RequestParam(value = "limit", defaultValue = "10") @Min(1) @Max(20) int limit,
                                     Principal principal) {
        return userDirectoryService.search(principal.getName(), query, limit);
    }
}

