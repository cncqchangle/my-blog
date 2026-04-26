package com.example.myblog.domain.view;

import java.time.LocalDateTime;

public record NoteDetailRow(
        Long noteId,
        String authorAccount,
        Long folderId,
        String folderName,
        String title,
        String markdownContent,
        String renderedHtml,
        LocalDateTime updatedAt) {
}

