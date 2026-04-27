package com.example.myblog.service;

import com.example.myblog.domain.Note;
import com.example.myblog.domain.command.CreateNoteRequest;
import com.example.myblog.domain.command.UpdateNoteRequest;
import com.example.myblog.domain.exception.ForbiddenOperationException;
import com.example.myblog.domain.exception.NotFoundException;
import com.example.myblog.domain.view.NoteSavedView;
import com.example.myblog.mapper.FolderMapper;
import com.example.myblog.mapper.NoteMapper;
import com.example.myblog.mapper.UserAccountMapper;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteCommandService {

    private static final Logger log = LoggerFactory.getLogger(NoteCommandService.class);

    private final UserAccountMapper userAccountMapper;
    private final FolderMapper folderMapper;
    private final NoteMapper noteMapper;
    private final MarkdownRenderService markdownRenderService;
    private final CoverImageStorageService coverImageStorageService;

    public NoteCommandService(UserAccountMapper userAccountMapper,
                              FolderMapper folderMapper,
                              NoteMapper noteMapper,
                              MarkdownRenderService markdownRenderService,
                              CoverImageStorageService coverImageStorageService) {
        this.userAccountMapper = userAccountMapper;
        this.folderMapper = folderMapper;
        this.noteMapper = noteMapper;
        this.markdownRenderService = markdownRenderService;
        this.coverImageStorageService = coverImageStorageService;
    }

    @Transactional
    public NoteSavedView create(String currentAccount, CreateNoteRequest request) {
        var user = userAccountMapper.findByAccount(currentAccount)
                .orElseThrow(() -> new NotFoundException("Current user not found"));
        var folder = folderMapper.findById(request.getFolderId())
                .orElseThrow(() -> new NotFoundException("Folder not found"));
        String normalizedTitle = request.getTitle().trim();
        if (!folder.getOwnerUserId().equals(user.getId())) {
            throw new ForbiddenOperationException("Target folder is not owned by current user");
        }

        Note note = new Note();
        note.setAuthorUserId(user.getId());
        note.setFolderId(request.getFolderId());
        note.setTitle(normalizedTitle);
        note.setCoverImageUrl(resolveCoverImageForCreate(user.getAccount(), normalizedTitle, request.getCoverImage()));
        note.setMarkdownContent(request.getMarkdownContent());
        note.setRenderedHtml(markdownRenderService.render(request.getMarkdownContent()));
        note.setPublicationStatus("PUBLISHED");
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.insert(note);
        return new NoteSavedView(note.getId(), note.getUpdatedAt(), note.getRenderedHtml(), note.getCoverImageUrl());
    }

    @Transactional
    public NoteSavedView update(String currentAccount, Long noteId, UpdateNoteRequest request) {
        var user = userAccountMapper.findByAccount(currentAccount)
                .orElseThrow(() -> new NotFoundException("Current user not found"));
        var note = noteMapper.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Note not found"));
        var folder = folderMapper.findById(request.getFolderId())
                .orElseThrow(() -> new NotFoundException("Folder not found"));
        String normalizedTitle = request.getTitle().trim();
        String currentCoverImageUrl = note.getCoverImageUrl();

        if (!note.getAuthorUserId().equals(user.getId())) {
            throw new ForbiddenOperationException("Current user is not the note author");
        }
        if (!folder.getOwnerUserId().equals(user.getId())) {
            throw new ForbiddenOperationException("Target folder is not owned by current user");
        }

        note.setFolderId(request.getFolderId());
        note.setTitle(normalizedTitle);
        note.setCoverImageUrl(resolveCoverImageForUpdate(user.getAccount(), normalizedTitle, request.getCoverImage(), currentCoverImageUrl));
        note.setMarkdownContent(request.getMarkdownContent());
        note.setRenderedHtml(markdownRenderService.render(request.getMarkdownContent()));
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.update(note);
        cleanupReplacedManagedCover(currentCoverImageUrl, note.getCoverImageUrl());
        return new NoteSavedView(note.getId(), note.getUpdatedAt(), note.getRenderedHtml(), note.getCoverImageUrl());
    }

    @Transactional
    public void delete(String currentAccount, Long noteId) {
        var user = userAccountMapper.findByAccount(currentAccount)
                .orElseThrow(() -> new NotFoundException("Current user not found"));
        var note = noteMapper.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Note not found"));

        if (!note.getAuthorUserId().equals(user.getId())) {
            throw new ForbiddenOperationException("Current user is not the note author");
        }

        noteMapper.deleteById(noteId);
        try {
            coverImageStorageService.deleteManagedCoverImage(note.getCoverImageUrl());
        } catch (RuntimeException ex) {
            log.warn("Failed to delete managed cover image for note {}", noteId, ex);
        }
    }

    private String resolveCoverImageForCreate(String account,
                                              String title,
                                              org.springframework.web.multipart.MultipartFile coverImage) {
        if (coverImage == null || coverImage.isEmpty()) {
            return coverImageStorageService.generateCoverImage(account, title);
        }
        return coverImageStorageService.uploadCoverImage(account, coverImage);
    }

    private String resolveCoverImageForUpdate(String account,
                                              String title,
                                              org.springframework.web.multipart.MultipartFile coverImage,
                                              String currentCoverImageUrl) {
        if (coverImage != null && !coverImage.isEmpty()) {
            return coverImageStorageService.uploadCoverImage(account, coverImage);
        }
        if (!org.springframework.util.StringUtils.hasText(currentCoverImageUrl)
                || coverImageStorageService.isGeneratedCoverImageUrl(currentCoverImageUrl)) {
            return coverImageStorageService.generateCoverImage(account, title);
        }
        return coverImageStorageService.resolveCoverImageUrl(currentCoverImageUrl);
    }

    private void cleanupReplacedManagedCover(String previousCoverImageUrl, String nextCoverImageUrl) {
        if (!org.springframework.util.StringUtils.hasText(previousCoverImageUrl)
                || previousCoverImageUrl.equals(nextCoverImageUrl)) {
            return;
        }
        try {
            coverImageStorageService.deleteManagedCoverImage(previousCoverImageUrl);
        } catch (RuntimeException ex) {
            log.warn("Failed to delete replaced managed cover image {}", previousCoverImageUrl, ex);
        }
    }
}

