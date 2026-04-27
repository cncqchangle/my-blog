package com.example.myblog.service;

import com.example.myblog.domain.Folder;
import com.example.myblog.domain.exception.ConflictException;
import com.example.myblog.domain.exception.ForbiddenOperationException;
import com.example.myblog.domain.exception.NotFoundException;
import com.example.myblog.domain.view.FolderCreatedResponse;
import com.example.myblog.mapper.FolderMapper;
import com.example.myblog.mapper.NoteMapper;
import com.example.myblog.mapper.UserAccountMapper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FolderService {

    private final FolderMapper folderMapper;
    private final NoteMapper noteMapper;
    private final UserAccountMapper userAccountMapper;

    public FolderService(FolderMapper folderMapper,
                         NoteMapper noteMapper,
                         UserAccountMapper userAccountMapper) {
        this.folderMapper = folderMapper;
        this.noteMapper = noteMapper;
        this.userAccountMapper = userAccountMapper;
    }

    @Transactional
    public FolderCreatedResponse createFolder(String currentAccount, String name) {
        var user = userAccountMapper.findByAccount(currentAccount)
                .orElseThrow(() -> new NotFoundException("Current user not found"));
        folderMapper.findByOwnerUserIdAndName(user.getId(), name.trim())
                .ifPresent(_ -> {
                    throw new ConflictException("Folder name already exists");
                });

        Folder folder = new Folder();
        folder.setOwnerUserId(user.getId());
        folder.setName(name.trim());
        folder.setDisplayOrder(folderMapper.nextDisplayOrder(user.getId()));
        folder.setCreatedAt(LocalDateTime.now());
        folder.setUpdatedAt(LocalDateTime.now());
        folderMapper.insert(folder);
        return new FolderCreatedResponse(folder.getId(), folder.getName());
    }

    @Transactional
    public void deleteFolder(String currentAccount, Long folderId) {
        var user = userAccountMapper.findByAccount(currentAccount)
                .orElseThrow(() -> new NotFoundException("Current user not found"));
        var folder = folderMapper.findById(folderId)
                .orElseThrow(() -> new NotFoundException("Folder not found"));

        if (!folder.getOwnerUserId().equals(user.getId())) {
            throw new ForbiddenOperationException("Current user does not own the folder");
        }
        if (noteMapper.countByFolderId(folderId) > 0) {
            throw new ConflictException("Folder cannot be deleted while it still contains notes");
        }

        folderMapper.deleteById(folderId);
    }
}
