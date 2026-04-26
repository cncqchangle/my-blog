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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteCommandService {

    private final UserAccountMapper userAccountMapper;
    private final FolderMapper folderMapper;
    private final NoteMapper noteMapper;
    private final MarkdownRenderService markdownRenderService;

    public NoteCommandService(UserAccountMapper userAccountMapper,
                              FolderMapper folderMapper,
                              NoteMapper noteMapper,
                              MarkdownRenderService markdownRenderService) {
        this.userAccountMapper = userAccountMapper;
        this.folderMapper = folderMapper;
        this.noteMapper = noteMapper;
        this.markdownRenderService = markdownRenderService;
    }

    @Transactional
    public NoteSavedView create(String currentAccount, CreateNoteRequest request) {
        var user = userAccountMapper.findByAccount(currentAccount)
                .orElseThrow(() -> new NotFoundException("Current user not found"));
        var folder = folderMapper.findById(request.folderId())
                .orElseThrow(() -> new NotFoundException("Folder not found"));
        if (!folder.getOwnerUserId().equals(user.getId())) {
            throw new ForbiddenOperationException("Target folder is not owned by current user");
        }

        Note note = new Note();
        note.setAuthorUserId(user.getId());
        note.setFolderId(request.folderId());
        note.setTitle(request.title().trim());
        note.setMarkdownContent(request.markdownContent());
        note.setRenderedHtml(markdownRenderService.render(request.markdownContent()));
        note.setPublicationStatus("PUBLISHED");
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.insert(note);
        return new NoteSavedView(note.getId(), note.getUpdatedAt(), note.getRenderedHtml());
    }

    @Transactional
    public NoteSavedView update(String currentAccount, Long noteId, UpdateNoteRequest request) {
        var user = userAccountMapper.findByAccount(currentAccount)
                .orElseThrow(() -> new NotFoundException("Current user not found"));
        var note = noteMapper.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Note not found"));
        var folder = folderMapper.findById(request.folderId())
                .orElseThrow(() -> new NotFoundException("Folder not found"));

        if (!note.getAuthorUserId().equals(user.getId())) {
            throw new ForbiddenOperationException("Current user is not the note author");
        }
        if (!folder.getOwnerUserId().equals(user.getId())) {
            throw new ForbiddenOperationException("Target folder is not owned by current user");
        }

        note.setFolderId(request.folderId());
        note.setTitle(request.title().trim());
        note.setMarkdownContent(request.markdownContent());
        note.setRenderedHtml(markdownRenderService.render(request.markdownContent()));
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.update(note);
        return new NoteSavedView(note.getId(), note.getUpdatedAt(), note.getRenderedHtml());
    }
}

