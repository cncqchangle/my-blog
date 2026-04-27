package com.example.myblog.domain.view;

import java.time.LocalDateTime;

public record NoteSavedView(Long noteId, LocalDateTime updatedAt, String renderedHtml, String coverImageUrl) {
}

