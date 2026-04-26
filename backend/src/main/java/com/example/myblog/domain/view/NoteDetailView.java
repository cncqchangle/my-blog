package com.example.myblog.domain.view;

import java.time.LocalDateTime;

public record NoteDetailView(
        Long noteId,
        String authorAccount,
        Long folderId,
        String folderName,
        String title,
        String markdownContent,
        String renderedHtml,
        boolean isEditable,
        LocalDateTime updatedAt) {
}

