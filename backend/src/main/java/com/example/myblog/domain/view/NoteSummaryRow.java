package com.example.myblog.domain.view;

import java.time.LocalDateTime;

public record NoteSummaryRow(
        Long noteId,
        String title,
        String coverImageUrl,
        String markdownContent,
        LocalDateTime updatedAt) {
}
