package com.example.myblog.service;

import com.example.myblog.domain.Folder;
import com.example.myblog.domain.exception.NotFoundException;
import com.example.myblog.domain.view.FolderView;
import com.example.myblog.domain.view.HomePageView;
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

    public HomePageService(UserAccountMapper userAccountMapper,
                           FolderMapper folderMapper,
                           NoteMapper noteMapper) {
        this.userAccountMapper = userAccountMapper;
        this.folderMapper = folderMapper;
        this.noteMapper = noteMapper;
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
        var notes = noteMapper.findSummariesByFolderId(folder.getId());
        return new FolderView(folder.getId(), folder.getName(), notes.size(), notes);
    }
}

