package com.example.myblog.service;

import com.example.myblog.domain.Folder;
import com.example.myblog.domain.exception.NotFoundException;
import com.example.myblog.domain.view.FolderView;
import com.example.myblog.domain.view.HomePageView;
import com.example.myblog.domain.view.NoteSummaryView;
import com.example.myblog.mapper.FolderMapper;
import com.example.myblog.mapper.NoteMapper;
import com.example.myblog.mapper.UserAccountMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HomePageService {

    private final UserAccountMapper userAccountMapper;
    private final FolderMapper folderMapper;
    private final NoteMapper noteMapper;
    private final NotePreviewService notePreviewService;
    private final CoverImageStorageService coverImageStorageService;

    public HomePageService(UserAccountMapper userAccountMapper,
                           FolderMapper folderMapper,
                           NoteMapper noteMapper,
                           NotePreviewService notePreviewService,
                           CoverImageStorageService coverImageStorageService) {
        this.userAccountMapper = userAccountMapper;
        this.folderMapper = folderMapper;
        this.noteMapper = noteMapper;
        this.notePreviewService = notePreviewService;
        this.coverImageStorageService = coverImageStorageService;
    }

    public HomePageView getOwnHomePage(String currentAccount) {
        return getHomePage(currentAccount, currentAccount);
    }

    public HomePageView getHomePage(String viewerAccount, String ownerAccount) {
        var owner = userAccountMapper.findByAccount(ownerAccount)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<FolderView> folders = folderMapper.findByOwnerUserId(owner.getId()).stream()
                .map(this::toFolderView)
                .toList();
        return new HomePageView(owner.getAccount(), owner.getAccount().equalsIgnoreCase(viewerAccount), folders);
    }

    private FolderView toFolderView(Folder folder) {
        List<NoteSummaryView> notes = noteMapper.findSummariesByFolderId(folder.getId()).stream()
                .map(row -> new NoteSummaryView(
                        row.noteId(),
                        row.title(),
                        coverImageStorageService.resolveCoverImageUrl(row.coverImageUrl(), row.title()),
                        notePreviewService.createPreview(row.markdownContent()),
                        row.updatedAt()))
                .toList();
        return new FolderView(folder.getId(), folder.getName(), notes.size(), notes);
    }
}

