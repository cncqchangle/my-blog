package com.example.myblog.controller;

import com.example.myblog.domain.command.CreateFolderRequest;
import com.example.myblog.domain.view.FolderCreatedResponse;
import com.example.myblog.service.FolderService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolderCreatedResponse createFolder(@Valid @RequestBody CreateFolderRequest request, Principal principal) {
        return folderService.createFolder(principal.getName(), request.name());
    }

    @DeleteMapping("/{folderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFolder(@PathVariable Long folderId, Principal principal) {
        folderService.deleteFolder(principal.getName(), folderId);
    }
}

