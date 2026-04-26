package com.example.myblog.service;

import com.example.myblog.domain.exception.NotFoundException;
import com.example.myblog.domain.view.NoteDetailRow;
import com.example.myblog.domain.view.NoteDetailView;
import com.example.myblog.mapper.NoteMapper;
import org.springframework.stereotype.Service;

@Service
public class NoteQueryService {

    private final NoteMapper noteMapper;

    public NoteQueryService(NoteMapper noteMapper) {
        this.noteMapper = noteMapper;
    }

    public NoteDetailView getNoteDetail(String currentAccount, Long noteId) {
        NoteDetailRow row = noteMapper.findDetailById(noteId)
                .orElseThrow(() -> new NotFoundException("Note not found"));
        boolean editable = row.authorAccount().equalsIgnoreCase(currentAccount);
        return new NoteDetailView(
                row.noteId(),
                row.authorAccount(),
                row.folderId(),
                row.folderName(),
                row.title(),
                row.markdownContent(),
                row.renderedHtml(),
                editable,
                row.updatedAt());
    }
}

