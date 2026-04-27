package com.example.myblog.domain.view;

import java.time.LocalDateTime;

public record NoteSummaryView(
        Long noteId,
        String title,
        String coverImageUrl,
        String contentPreview,
        LocalDateTime updatedAt) {
}

